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
 * actor might accidentally be started in different shard regions at the same time.
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

	public ShardMessageExtractor() {
		this.cardinality = 0;
	}

	@Inject
	public ShardMessageExtractor(@ConfigProperty(name = CARDINALITY_PROP, defaultValue = CARDINALITY_DEFAULT) int cardinality) {
		this.cardinality = cardinality;
	}

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
		final Class<?> clazz = message.getClass();
		final String name = clazz.getName();
		return name;
	}
}
