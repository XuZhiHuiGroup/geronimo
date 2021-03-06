/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.rmi.iiop;

public final class StringSeqHolder implements org.omg.CORBA.portable.Streamable
{
    public java.lang.String[] value;

    public StringSeqHolder()
    {
    }

    public StringSeqHolder
        (java.lang.String[] _value)
    {
        value = _value;
    }

    public org.omg.CORBA.TypeCode _type()
    {
        return org.apache.geronimo.interop.rmi.iiop.StringSeqHelper.type();
    }

    public void _read
        (org.omg.CORBA.portable.InputStream _input)
    {
        value = org.apache.geronimo.interop.rmi.iiop.StringSeqHelper.read(_input);
    }

    public void _write
        (org.omg.CORBA.portable.OutputStream _output)
    {
        org.apache.geronimo.interop.rmi.iiop.StringSeqHelper.write(_output, value);
    }
}
