/**
 * Copyright © 2016-2025 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.common.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copy of Executors.DefaultThreadFactory but with ability to set name of the pool
 */
public class ThreadFactory implements java.util.concurrent.ThreadFactory {
	public static final String THREAD_TOPIC_SEPARATOR = " | ";
	private static final AtomicInteger poolNumber = new AtomicInteger(1);
	private final ThreadGroup group;
	private final AtomicInteger threadNumber = new AtomicInteger(1);
	private final String namePrefix;

	private ThreadFactory(String name) {
		SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
		namePrefix = name + "-" + poolNumber.getAndIncrement() + "-";
	}

	public static ThreadFactory forName(String name) {
		return new ThreadFactory(name);
	}

	public static void updateCurrentThreadName(String threadSuffix) {
		String name = Thread.currentThread().getName();
		int spliteratorIndex = name.indexOf(THREAD_TOPIC_SEPARATOR);
		if (spliteratorIndex > 0) {
			name = name.substring(0, spliteratorIndex);
		}
		name = name + THREAD_TOPIC_SEPARATOR + threadSuffix;
		Thread.currentThread().setName(name);
	}


	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
		if (t.isDaemon()) {
			t.setDaemon(false);
		}
		if (t.getPriority() != Thread.NORM_PRIORITY) {
			t.setPriority(Thread.NORM_PRIORITY);
		}
		return t;
	}
}
