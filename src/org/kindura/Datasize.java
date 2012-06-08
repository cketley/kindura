package org.kindura;

/*
Copyright 2012 Cheney Ketley, employee of Science & Technology Facilities Council and
subcontracted to Kings College London.
This file is part of Kindura.

Kindura is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Kindura is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Kindura.  If not, see <http://www.gnu.org/licenses/>.
 */

public enum Datasize { TiB("TiB"), TB("TB"), GiB("GiB"), GB("GB"), MiB("MiB"), MB("MB"), KB("KB"), KiB("KiB"), B("B"), PiB("PiB"), PB("PB"), missing("") ;

private String value;

private Datasize(String value) {
	this.value = value;
}

}

