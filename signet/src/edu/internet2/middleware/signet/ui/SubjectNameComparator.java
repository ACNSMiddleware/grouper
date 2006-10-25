/*--
$Id: SubjectNameComparator.java,v 1.3 2006-10-25 00:09:40 ddonn Exp $
$Date: 2006-10-25 00:09:40 $

Copyright 2006 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package edu.internet2.middleware.signet.ui;

import java.util.Comparator;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;

public class SubjectNameComparator implements Comparator {

	/**
	 * Compares Subjects by their names.
	 */
	public SubjectNameComparator() {
		super();
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		if (o1 instanceof SignetSubject
			&& o2 instanceof SignetSubject) {
			String name1 = ((SignetSubject)o1).getName().toLowerCase();
			String name2 = ((SignetSubject)o2).getName().toLowerCase();
			return name1.compareTo(name2);
		}
		return 0;
	}

}
