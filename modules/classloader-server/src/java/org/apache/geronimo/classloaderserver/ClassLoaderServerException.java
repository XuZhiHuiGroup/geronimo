/**
 *
 * Copyright 2005 The Apache Software Foundation
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

package org.apache.geronimo.classloaderserver;


/**
 *
 * @version $Rev: 109957 $ $Date: 2004-12-06 18:52:06 +1100 (Mon, 06 Dec 2004) $
 */
public class ClassLoaderServerException extends Exception {

    public ClassLoaderServerException() {
    }

    public ClassLoaderServerException(Throwable cause) {
        super(cause);
    }

    public ClassLoaderServerException(String message) {
        super(message);
    }

    public ClassLoaderServerException(String message, Throwable cause) {
        super(message, cause);
    }
    
}

