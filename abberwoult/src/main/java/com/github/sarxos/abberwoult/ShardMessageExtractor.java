package com.github.sarxos.abberwoult;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import akka.cluster.sharding.ShardRegion.MessageExtractor;
import io.quarkus.runtime.annotations.Template;


/**
 * The class defines methods to extract the entity ID and the shard ID from incoming messages. A
 * shard is a group of actor entities which are managed together. The grouping is defined by the
 * shard ID. For a specific entity ID the shard ID must always be the same. Otherwise the entity
 * actor might accidentally be started in different shard regions at the same time.<br><br>
 * 
 * It's annotated with both {@link Template} and {@link Singleton} but p[lease note that it's
 * either one or the other at the given time. It's never both. At the augmentation time this class
 * acts as a recorder {@link Template} and in runtime it's a {@link Singleton} bean. Please keep
 * this in mind and don't be fooled with the presence of these two annotations. It cannot be both
 * in the same time because CDI is not yet initialized in augmentation time and therefore there
 * is no concept of {@link Singleton} bean then.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Template
@Singleton
public class ShardMessageExtractor implements MessageExtractor {

	private static final Logger LOG = Logger.getLogger(ShardMessageExtractor.class);

	private static final Map<String, MessageExtractor> EXTRACTORS = new HashMap<>();
	private static final String CARDINALITY_PROP = "akka.cluster.sharding.cardinality";
	private static final String CARDINALITY_DEFAULT = "100";

	private final int cardinality;

	/**
	 * Default constructor to be used when this class is used as a recorder {@link Template}.
	 */
	public ShardMessageExtractor() {
		this.cardinality = 0;
	}

	/**
	 * Injectable constructor to be used when this class is used as a {@link Singleton} bean. 
	 *
	 * @param cardinality the sharding cardinality (maximum number of shard regions in cluster)
	 */
	@Inject
	public ShardMessageExtractor(@ConfigProperty(name = CARDINALITY_PROP, defaultValue = CARDINALITY_DEFAULT) int cardinality) {
		this.cardinality = cardinality;
	}

	/**
	 * Recording method used when this is {@link Template} to register {@link MessageExtractor}
	 * for a given message class name.
	 *
	 * @param clazz the message class name
	 * @param extractor the {@link MessageExtractor} to be used to extract shard and entity IDs
	 */
	public void register(final String clazz, final MessageExtractor extractor) {
		LOG.debugf("Record synthetic message extractor %s for class %s", extractor.getClass(), clazz);
		EXTRACTORS.put(clazz, extractor);
	}

	@Override
	public String shardId(final Object message) {

		if (cardinality == 0) {
			return StringUtils.EMPTY;
		}

		final String key = key(message);
		final MessageExtractor extractor = EXTRACTORS.get(key);
		final String value = extractor.shardId(message);
		final int hash = Math.abs(Objects.hash(value));

		return Integer.toString(hash % cardinality);
	}

	@Override
	public String entityId(final Object message) {

		if (cardinality == 0) {
			return StringUtils.EMPTY;
		}

		return EXTRACTORS
			.get(key(message))
			.entityId(message);
	}

	@Override
	public Object entityMessage(final Object message) {
		if (cardinality == 0) {
			return null;
		} else {
			return message;
		}
	}

	private String key(final Object message) {
		return  message.getClass().getName();
	}
}
