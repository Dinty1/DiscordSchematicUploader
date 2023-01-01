/*-
 * LICENSE
 * DiscordSchematicUploader
 * -------------
 * Copyright (C) 2021 - 2023 Dinty1
 * -------------
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * END
 */

package io.github.dinty1.discordschematicuploader.util;

import io.github.dinty1.discordschematicuploader.DiscordSchematicUploader;
import net.querz.nbt.io.NamedTag;

public class SchematicAuditUtil {
    public static boolean containsBlockedPhrase(NamedTag nbt) {
        for (final String phrase : DiscordSchematicUploader.getPlugin().getConfig().getStringList("schematic-upload-phrase-blacklist")) {
            if (nbt.getTag().valueToString().toLowerCase().contains(phrase.toLowerCase())) return true;
        }
        return false;
    }
}
