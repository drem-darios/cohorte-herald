/**
 * Copyright 2015 isandlaTech
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

package org.cohorte.herald.pyapi.impl;

import org.cohorte.herald.pyapi.IEvent;
import org.cohorte.herald.pyapi.IEventFactory;

/**
 * Java implementation of the Event factory
 *
 * @author Thomas Calmant
 */
public class EventFactory implements IEventFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cohorte.herald.pyapi.IEventFactory#createEvent()
	 */
	@Override
	public IEvent createEvent() {
		return new Event();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cohorte.herald.pyapi.IEventFactory#sleep(long)
	 */
	@Override
	public void sleep(final long aMilliseconds) {
		try {
			Thread.sleep(aMilliseconds);
		} catch (final InterruptedException ex) {
			// Do nothing
		}
	}
}
