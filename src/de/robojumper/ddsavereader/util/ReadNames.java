package de.robojumper.ddsavereader.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

// "Commandlet" that compiles a list of Names to use for the Hash Table from the game files (and mods)
// No output file, just pipe it to the output file
public class ReadNames {

	static final Set<String> NAMES = new HashSet<String>();
	// args is a list of game or mod root directories
	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			File RootDir = new File(args[i]);
			if (RootDir.isDirectory()) {
				// parse monster list
				List<File> allDirs = new ArrayList<File>(Arrays.asList(RootDir.listFiles()));
				for (int j = 0; j < allDirs.size(); j++) {
					File[] files = allDirs.get(j).listFiles();
					if (files != null) {
						allDirs.addAll(Arrays.asList(files));
					}
					if (allDirs.get(j).isDirectory()) {
						if (allDirs.get(j).getName().equalsIgnoreCase("monsters")) {
							ParseMonsterDirectory(allDirs.get(j));
						}
					}
				}
				// parse upgrade trees
				ParseUpgradeTrees(RootDir);
			}
		}
		Iterator<String> it = NAMES.iterator();
		while (it.hasNext()) {
			System.out.println(it.next());
		}
	}
	
	private static void ParseUpgradeTrees(File directory) {
		try {
			Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>() {
				@Override
			    public FileVisitResult preVisitDirectory(Path dir,
			            BasicFileAttributes attrs) {
			        return FileVisitResult.CONTINUE;
			    }

			    @Override
			    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			        if (file.toString().endsWith(".upgrades.json")) {
			        	try {
							String JsonString = new String(Files.readAllBytes(file));
							JsonParser parser = new JsonParser();
							JsonObject rootObject = parser.parse(JsonString).getAsJsonObject();
							JsonArray arrTrees = rootObject.getAsJsonArray("trees");
							if (arrTrees != null) {
								for (int i = 0; i < arrTrees.size(); i++) {
									String id = arrTrees.get(i).getAsJsonObject().get("id").getAsString();
									NAMES.add(id);
								}
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			        }
			        return FileVisitResult.CONTINUE;
			    }

			    @Override
			    public FileVisitResult visitFileFailed(Path file, IOException e) {
			        return FileVisitResult.CONTINUE;
			    }
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// find all "*.info.darkest" file, this file name without extension is the monster ID to be used
	private static void ParseMonsterDirectory(File MonsterDir) {
		try {
			Files.walkFileTree(MonsterDir.toPath(), new SimpleFileVisitor<Path>() {
				@Override
			    public FileVisitResult preVisitDirectory(Path dir,
			            BasicFileAttributes attrs) {
			        return FileVisitResult.CONTINUE;
			    }

			    @Override
			    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			        if (file.toString().endsWith(".info.darkest")) {
			        	String FileName = file.toFile().getName();
			        	if (FileName.indexOf(".") > 0) {
			        		FileName = FileName.substring(0, FileName.indexOf("."));
			        	}
			        	NAMES.add(FileName);    
			        }
			        return FileVisitResult.CONTINUE;
			    }

			    @Override
			    public FileVisitResult visitFileFailed(Path file, IOException e) {
			        return FileVisitResult.CONTINUE;
			    }
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}