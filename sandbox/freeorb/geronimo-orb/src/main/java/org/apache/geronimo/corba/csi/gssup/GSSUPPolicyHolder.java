/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
package org.apache.geronimo.corba.csi.gssup;

/**
 * Generated by the JacORB IDL compiler.
 */
public class GSSUPPolicyHolder implements org.omg.CORBA.portable.Streamable {

    public GSSUPPolicy value;

    public GSSUPPolicyHolder() {
    }

    public GSSUPPolicyHolder(GSSUPPolicy initial) {
        value = initial;
    }

    public org.omg.CORBA.TypeCode _type() {
        return GSSUPPolicyHelper.type();
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = GSSUPPolicyHelper.read(in);
    }

    public void _write(org.omg.CORBA.portable.OutputStream _out) {
        GSSUPPolicyHelper.write(_out, value);
    }
}
