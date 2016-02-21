package com.ellisvlad.protectionPlugin.Regions;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.util.Map.Entry;

import org.bukkit.Location;

import net.minecraft.server.v1_8_R3.RegionFile;
import net.minecraft.server.v1_8_R3.RegionFileCache;

public class RegionStorage {
	
	public static void test(Location pos1, Location pos2) {
		try {
			Field RAFfield=RegionFile.class.getDeclaredField("c");
			RAFfield.setAccessible(true);
		
			int minChunkX=Math.min(pos1.getBlockX()>>4, pos2.getBlockX()>>4);
			int maxChunkX=Math.max(pos1.getBlockX()>>4, pos2.getBlockX()>>4);
			int minChunkZ=Math.min(pos1.getBlockZ()>>4, pos2.getBlockZ()>>4);
			int maxChunkZ=Math.max(pos1.getBlockZ()>>4, pos2.getBlockZ()>>4);
			
			for (int chunkX=minChunkX; chunkX<=maxChunkX; chunkX++) {
				for (int chunkZ=minChunkZ; chunkZ<=maxChunkZ; chunkZ++) {
					RegionFile region=RegionFileCache.a(pos1.getWorld().getWorldFolder(), chunkX, chunkZ);
					RandomAccessFile file=(RandomAccessFile)RAFfield.get(region);
					//file.seek(pos);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		};
	}
	
}
