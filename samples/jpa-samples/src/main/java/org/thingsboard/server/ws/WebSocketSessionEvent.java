package org.thingsboard.server.ws;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public class WebSocketSessionEvent extends ApplicationEvent {
	public WebSocketSessionEvent(Object source) {
		super(source);
	}

	@AllArgsConstructor
	@Data
	static class WebSocketSessionEventSource {
		WebSocketSessionRef sessionRef;
		SessionEvent event;
	}
}
