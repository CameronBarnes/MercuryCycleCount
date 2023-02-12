/*
 *     Copyright (c) 2022.  Cameron Barnes
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.cameronbarnes.mercury.util;

import com.cameronbarnes.mercury.core.Main;
import com.cameronbarnes.mercury.core.Options;
import com.cameronbarnes.mercury.stock.Bin;
import com.cameronbarnes.mercury.stock.Part;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SerializationUtils {
	
	public static String serializeOptions(Options options) {
		
		return new Gson().toJson(options);
	}
	
	public static Optional<Options> deserializeOptions(String str) {
		
		try {
			Gson gson = new Gson();
			Options options = gson.fromJson(str, Options.class);
			options.generateFont();
			options.ensureAllNewPropertiesArePresent();
			return Optional.of(options);
		}
		catch (Exception e) { // It is possible that this could fail, and I want to make sure we cover that case
			return Optional.empty();
		}
		
	}
	
	public static String serializeVersion(Main.Version version) {
		
		return new Gson().toJson(version);
	}
	
	public static Optional<Main.Version> deserializeVersion(String str) {
		
		if (str == null || str.isBlank())
			return Optional.empty();
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Main.Version.class, new VersionDeserializer());
		Gson gson = builder.create();
		return Optional.ofNullable(gson.fromJson(str, Main.Version.class));
		
	}
	
	public static String serializePartList(List<Part> parts) {
		
		Gson gson = new GsonBuilder().serializeNulls().create();
		return gson.toJson(parts);
	}
	
	public static String serializeBin(Bin bin) {
		
		JsonObject json = new JsonObject();
		json.addProperty("BinNumber", bin.getBinNum());
		json.addProperty("WareHouse", bin.getWarehouse());
		json.addProperty("Parts", serializePartList(bin.getParts()));
		
		return json.toString();
		
	}
	
	public static List<Part> deserializePartList(String str) {
		
		Type type = new TypeToken<ArrayList<Part>>() {
		}.getType();
		return new Gson().fromJson(str, type);
		
	}
	
	public static Optional<Bin> deserializeBin(String bin) {
		return Optional.ofNullable(new GsonBuilder().registerTypeAdapter(Bin.class, new BinDeserializer()).create().fromJson(bin, Bin.class));
	}
	
	private static final class BinDeserializer implements JsonDeserializer<Bin> {
		
		@Override
		public Bin deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			return new Bin(jsonObject.get("BinNumber").getAsString(), jsonObject.get("WareHouse").getAsString(), deserializePartList(jsonObject.get("Parts").getAsString()), null);
		}
		
	}
	
	private static final class VersionDeserializer implements JsonDeserializer<Main.Version> {
		
		@Override
		public Main.Version deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();
			return new Main.Version(jsonObject.get("major").getAsInt(), jsonObject.get("minor").getAsInt(), jsonObject.get("patch").getAsInt(), Main.ReleaseType.valueOf(jsonObject.get("label").getAsString()));
		}
		
	}
	
}