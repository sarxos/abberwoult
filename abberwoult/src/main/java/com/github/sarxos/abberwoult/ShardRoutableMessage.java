package com.github.sarxos.abberwoult;

import java.io.Serializable;


/**
 * Interface to be implemented by all shard routable messages (a messages which are meant to be
 * dispatched to the shard actors). It's to ensure that all shard messages are {@link Serializable}
 * which is the requirement enforced by sharding. 
 *
 * @author Bartosz Firyn (sarxos)
 */
public interface ShardRoutableMessage extends Serializable {
}
