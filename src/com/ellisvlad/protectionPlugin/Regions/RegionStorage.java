package com.ellisvlad.protectionPlugin.Regions;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.Server.Spigot;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.ChunkProviderServer;
import net.minecraft.server.v1_8_R3.ChunkRegionLoader;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.RegionFile;
import net.minecraft.server.v1_8_R3.RegionFileCache;
import net.minecraft.server.v1_8_R3.WorldServer;

public class RegionStorage {
	
	@SuppressWarnings({ "unchecked", "unchecked" })
	public static void test(Location pos1, Location pos2) {
		try {
			Field RAFfield=RegionFile.class.getDeclaredField("c");
			RAFfield.setAccessible(true);
			Field ChunkServerfield=ChunkProviderServer.class.getDeclaredField("chunkLoader");
			ChunkServerfield.setAccessible(true);
			Field loadedTagsField=ChunkRegionLoader.class.getDeclaredField("b");
			loadedTagsField.setAccessible(true);
		
			int minChunkX=Math.min(pos1.getBlockX()>>4, pos2.getBlockX()>>4);
			int maxChunkX=Math.max(pos1.getBlockX()>>4, pos2.getBlockX()>>4);
			int minChunkZ=Math.min(pos1.getBlockZ()>>4, pos2.getBlockZ()>>4);
			int maxChunkZ=Math.max(pos1.getBlockZ()>>4, pos2.getBlockZ()>>4);
			
			for (int chunkX=minChunkX; chunkX<=maxChunkX; chunkX++) {
				for (int chunkZ=minChunkZ; chunkZ<=maxChunkZ; chunkZ++) {
					RegionFile region=RegionFileCache.a(pos1.getWorld().getWorldFolder(), chunkX, chunkZ);
					RandomAccessFile file=(RandomAccessFile)RAFfield.get(region);
					int relChunkX=chunkX&31, relChunkZ=chunkZ&31;
					
					synchronized (file) {
						((CraftWorld)pos1.getWorld()).getHandle().chunkProviderServer.saveChunk(((CraftChunk)pos1.getChunk()).getHandle());
						
						ChunkRegionLoader chunkLoader=(ChunkRegionLoader)ChunkServerfield.get(((CraftWorld)pos1.getWorld()).getHandle().chunkProviderServer);
						Map<ChunkCoordIntPair, NBTTagCompound> loadedTags=(Map<ChunkCoordIntPair, NBTTagCompound>)loadedTagsField.get(chunkLoader);
						
						NBTTagCompound nbt=(NBTTagCompound)loadedTags.get(new ChunkCoordIntPair(chunkX, chunkZ));
				        if (nbt==null||true) {
				            DataInputStream datainputstream=RegionFileCache.c(new File(pos1.getWorld().getName()), chunkX, chunkZ);
				            nbt=NBTCompressedStreamTools.a(datainputstream);
				        }
				        System.out.println(nbt);
				        
				        NBTTagCompound nbt2=new NBTTagCompound();
				        nbt2.setString("Hello", "World");
				        nbt.set("Test", nbt2);
				        DataOutputStream dos=RegionFileCache.d(new File(pos1.getWorld().getName()), chunkX, chunkZ);
				        NBTCompressedStreamTools.a(nbt, (OutputStream)dos);
				        System.out.println(nbt);
				        System.out.println("DONE");
				        
				        //return this.a(world, i, j, nbttagcompound);
						//file.seek((x+z*32)*4);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		};
	}
	
}
