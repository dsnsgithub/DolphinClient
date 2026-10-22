/*
 * Copyright © 2026 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.dolphinclient.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Month;
import java.time.MonthDay;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Optional;

public class AltIcons {
	/**
	 * <pre>
	 * ::::...';::::::::llodxxxxxxxxxxxxxxxxxxdoll::::::::;,......,
	 * :::,......,;:loxxxxxxxxxxdoooooooodxxxxxxxxxxoc,'...,ok0Kx..
	 * :::,........oxxxxxxxkoc:::::::::::::::ldxxxxdc'...lOXNNNNK..
	 * :::,.......coodxxxxxkxddddollc::lolc:cldxxo;....oKNNNNNNNK..
	 * ::::.......'looodxxxxxxxxxxxxxxdokxxkkxxl'.....kNNNNNNNNN0..
	 * :::;.........,:looodxxkxxxxxxxxxxxxxxxc.......dNNNNNNNNNNx..
	 * ::::,..........;ldxxxxxxxxxxxxxxxxxxx;.......cXNNNNNNNNNX'.'
	 * ::::;..........;cllodddddddddxxdddddc.......c0OKNNNNNNNXo..,
	 * :::::'......:oddddddddddddddddddddddddooooddddONNNNNNNXo...:
	 * ::::::...'cddddddddddddddddddddddddddddddddddxXNNNXKNXl...,:
	 * ::::::;.cddddocccloddddddddddddddddddddddddddxkkkxddXc....::
	 * ::::::codddl'oKNNk.,odddddddddddddddddddddddddddddddd....,::
	 * :::::cddddl.kMMMMN...odddddddddddddl:;;:lddddddddddd;...co::
	 * ::::lddddd:.:KNKx.;'.cdddddddddddl'kNMMN,.cdddddddddc..cddc:
	 * :::lddddddo....lkXM;.oddddddddddl.kMMMMW,..cdddddddddoodddo:
	 * ::cddddddddo:..kKOc.:dddddddddddc.'k0Oo.oo.:ddddddddddddddd:
	 * :cddddddddddddol,.'lddddddddddddd,...x0WMO'odddddddddddddddl
	 * :oddddddddddddo.'ldollloddddddddddl;.oOxlcdddddddddddddddddl
	 * lddddddddddddd,'ddo......'lddddddddddddddddddddddddddddddddl
	 * oddddddddddddd;.oddc.....:odddddddddddddddddddddddddddddddd:
	 * odddddddddddddocddddo::lddddddddddddddddddddddddddddddddddo:
	 * ddddddddddddd;.ldddddlodddddddddddddddddddddddddddddddddddc:
	 * dddddddddddddo.;dddl'.'oddddddc:dddddddddddddddddddddddddd::
	 * ddddddddddddddc.:l'.,l,.ldddl'.:dddddddddddddddddddddddddd::
	 * ddddddddddddddd;..;lddo'.:c'.;odddddddddddddddddddddddddddl:
	 * ddddddddddddddddoddddddd:.':odddddddddddddddddddddddddddddd:
	 * ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd:
	 * ldddddddddddddddddddddddddddddddddddddddddddddddddddddddddc:
	 * ::odddddddddddddddddddddddddddddddddddddddddddddddddddddl:::
	 * ::::clddddddddddddddddddddddddddddddddddddddddddddddolc:::::
	 * ::::::::loodddddddddddddddddddddddddddddddddddddollc::::::::
	 * ::::::::::::ccllooodddddddddddddddddddooolllcc::::::::::::::
	 * </pre>
	 */
	public static Optional<InputStream> getAltIcon() {
		var current = MonthDay.from(ZonedDateTime.now());
		if (current.getMonth().equals(Month.JUNE)) {
			return Optional.ofNullable(read("@FOX_PRIDE@"));
		}
		var tvd = MonthDay.of(Month.MARCH, 31);
		var tdor = MonthDay.of(Month.NOVEMBER, 20);
		if (current.equals(tvd) || current.equals(tdor)) {
			return Optional.ofNullable(read("@FOX_TRANS@"));
		}
		return Optional.empty();
	}

	private static InputStream read(String s) {
		if (s.contains("@")) return null;
		return Base64.getDecoder().wrap(new ByteArrayInputStream(s.getBytes(StandardCharsets.ISO_8859_1)));
	}
}
