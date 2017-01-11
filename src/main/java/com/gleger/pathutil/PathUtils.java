package com.gleger.pathutil;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class PathUtils {

	private PathUtils() {}
	
	public static boolean isEmptyDirectory(Path p) throws IOException {
		checkDirectory(p);
		try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(p)) {
			return !directoryStream.iterator().hasNext();
		}
	}
	
	public static long directorySize(Path p) throws IOException {
		checkDirectory(p);
		final AtomicLong size = new AtomicLong(0L);
		Files.walkFileTree(p, new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				size.addAndGet(Files.size(file));
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				throw new IOException("Unable to read file size for path " + file.toString(), exc);
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
		return size.get();
	}
	
	public static void cleanDirectory(Path dir) throws IOException {
		checkDirectory(dir);
		try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
			for(Path p : stream) {
				if(Files.isDirectory(p)) {
					deleteDirectoryRecursively(p);
				} else {
					Files.delete(p);
				}
			}
		}
	}
	
	public static void deleteDirectoryRecursively(Path directoryPath) throws IOException {
		checkDirectory(directoryPath);
		Files.walkFileTree(directoryPath, new FileVisitor<Path>() {

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				throw exc;
			}
		});
	}
	
	public static List<Path> listFilesInDirectory(Path directory) throws IOException {
		checkDirectory(directory);
		ArrayList<Path> files = new ArrayList<>();
		Files.walkFileTree(directory, new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				files.add(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				throw exc;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
		return files;
	}
	
	public static void copyDirectoryToDirectory(final Path sourceDir, final Path destDir) throws IOException {
		if(!Files.isDirectory(sourceDir)) {
			throw new IOException("Source path is not a directory: " + sourceDir.toString());
		}
		if(!Files.isDirectory(destDir)) {
			throw new IOException("Destination path is not a directory: " + destDir.toString());
		}
		Files.walkFileTree(sourceDir, new FileVisitor<Path>() {

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Path relativePath = sourceDir.relativize(sourceDir);
				Path resolvedDir = destDir.resolve(relativePath);
				if(!Files.exists(resolvedDir))  {
					Files.createDirectory(resolvedDir);
				}
				if(!Files.isDirectory(resolvedDir)) {
					throw new IOException("Can't create directory " + resolvedDir.toString() + ", it already exists and is not a directory");
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Path relativePath = sourceDir.relativize(file);
				Files.copy(file, destDir.resolve(relativePath));
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				throw exc;
			}
		});
	}

	public static void moveDirectoryToDirectory(final Path sourceDir, final Path destDir) throws IOException {
		if(!Files.isDirectory(sourceDir)) {
			throw new IOException("Source path is not a directory: " + sourceDir.toString());
		}
		if(!Files.isDirectory(destDir)) {
			throw new IOException("Destination path is not a directory: " + destDir.toString());
		}
		Files.walkFileTree(sourceDir, new FileVisitor<Path>() {
			
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				if(exc != null) {
					throw exc;
				}
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Path relativePath = sourceDir.relativize(sourceDir);
				Files.createDirectory(destDir.resolve(relativePath));
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Path relativePath = sourceDir.relativize(file);
				Files.copy(file, destDir.resolve(relativePath));
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				throw exc;
			}
		});
	}
	
	private static void checkDirectory(Path directoryPath) throws IOException {
		if(!Files.isDirectory(directoryPath)) {
			throw new IOException("Specified Path: " + directoryPath.toString() + " is not a directory");
		}
	}
}
