package com.gleger.pathutil.filter;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public enum DirectoryStreamFileFilters implements DirectoryStream.Filter<Path>{

	DIRECTORY {
		public boolean accept(Path entry) throws IOException {
			return Files.isDirectory(entry);
		}
	}, 
	FILES_ONLY {
		public boolean accept(Path entry) throws IOException {
			return !Files.isDirectory(entry);
		}
	}, 
	READABLE {
		public boolean accept(Path entry) throws IOException {
			return Files.isReadable(entry);
		}
	},
	WRITABLE {
		public boolean accept(Path entry) throws IOException {
			return Files.isWritable(entry);
		}
	}
}
