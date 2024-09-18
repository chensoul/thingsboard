package org.thingsboard.queue.msg;

import java.io.Serializable;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public class ToCoreMsg implements Serializable {
	String msg;

	public ToCoreMsg(String msg) {
		this.msg = msg;
	}

	@Override
	public String toString() {
		return msg;
	}
}
