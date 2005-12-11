/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.kernel;

/**
 * An interface with a couple more or less arbitrary methods, used to
 * test GBeans declaring interfaces and generating proxies based on
 * those interfaces.
 *
 * @version $Rev$ $Date$
 */
public interface MockChildInterface2 extends MockChildInterface1 {
    public String doSomething(String name);

    public void doNothing();
}
