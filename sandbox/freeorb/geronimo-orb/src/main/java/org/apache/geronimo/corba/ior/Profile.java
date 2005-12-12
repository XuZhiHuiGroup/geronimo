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
package org.apache.geronimo.corba.ior;

import org.omg.CORBA.OctetSeqHelper;
import org.omg.CORBA.portable.OutputStream;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TAG_MULTIPLE_COMPONENTS;
import org.omg.IOP.TaggedComponent;
import org.omg.IOP.TaggedProfile;

import org.apache.geronimo.corba.AbstractORB;
import org.apache.geronimo.corba.ORB;
import org.apache.geronimo.corba.io.EncapsulationOutputStream;
import org.apache.geronimo.corba.io.GIOPVersion;
import org.apache.geronimo.corba.io.OutputStreamBase;

public abstract class Profile extends TaggedValue {

	public Profile() {
	}

	public void write_encapsulated_content(OutputStreamBase out) {
		byte[] bytes = get_encapsulation_bytes();
		if (bytes == null) {
			super.write_encapsulated_content(out);
		} else {
			out.write_octet_array(bytes, 0, bytes.length);
		}
	}

	/**
	 * write content including tag
	 */
	public void write(OutputStream out) {
		out.write_long(tag());
		OctetSeqHelper.write(out, get_encapsulation_bytes());
	}

	abstract protected byte[] get_encapsulation_bytes();

	public static Profile read(AbstractORB orb, int tag, byte[] data) {

		switch (tag) {
		case TAG_INTERNET_IOP.value:
			return IIOPProfile.read(orb, data);

		case TAG_MULTIPLE_COMPONENTS.value:
			return MultiComponentProfile.read(orb, data);

		default:
			return new UnknownProfile(tag, data);
		}
	}

	public abstract int getComponentCount();

	public abstract int getTag(int idx);

	public abstract TaggedComponent getTaggedComponent(int idx);

	public abstract Component getComponent(int idx);

	public TaggedProfile asTaggedProfile(ORB orb) {
		byte[] bytes = get_encapsulation_bytes();
		if (bytes == null) {
			EncapsulationOutputStream eos = new EncapsulationOutputStream(
					orb, GIOPVersion.V1_0);
			super.write_encapsulated_content(eos);
			bytes = eos.getBytes();
		}
		TaggedProfile tp = new TaggedProfile(tag(), bytes);
		return tp;
	}

}
