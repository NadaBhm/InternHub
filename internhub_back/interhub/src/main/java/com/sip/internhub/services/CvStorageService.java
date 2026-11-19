package com.sip.internhub.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CvStorageService {

	private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
	private static final int PDF_HEADER_LENGTH = 5;

	private final Path uploadDirectory;

	public CvStorageService(@Value("${app.upload-dir:uploads}") String uploadDirectory) {
		this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
	}

	public StoredCv store(MultipartFile file) throws IOException {
		if (file == null || file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Veuillez joindre votre CV au format PDF");
		}
		if (file.getSize() > MAX_FILE_SIZE) {
			throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Le CV ne doit pas dépasser 5 Mo");
		}

		String originalFilename = file.getOriginalFilename();
		String safeFilename = sanitizeFilename(originalFilename);
		if (safeFilename == null || !safeFilename.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le fichier du CV doit avoir l'extension .pdf");
		}

		try (InputStream input = file.getInputStream()) {
			byte[] header = input.readNBytes(PDF_HEADER_LENGTH);
			if (header.length != PDF_HEADER_LENGTH
					|| header[0] != '%' || header[1] != 'P' || header[2] != 'D'
					|| header[3] != 'F' || header[4] != '-') {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le fichier envoyé n'est pas un PDF valide");
			}
		}

		Files.createDirectories(uploadDirectory);
		String storageKey = UUID.randomUUID().toString() + ".pdf";
		Path destination = uploadDirectory.resolve(storageKey).normalize();
		if (!destination.getParent().equals(uploadDirectory)) {
			throw new IllegalStateException("Invalid CV storage path");
		}
		try (InputStream input = file.getInputStream()) {
			Files.copy(input, destination);
		}
		return new StoredCv(storageKey, safeFilename);
	}

	public Path resolve(String storageKey) {
		Path file = resolveStorageKey(storageKey);
		if (!Files.isRegularFile(file)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV introuvable");
		}
		return file;
	}

	public void delete(String storageKey) throws IOException {
		Files.deleteIfExists(resolveStorageKey(storageKey));
	}

	private Path resolveStorageKey(String storageKey) {
		if (storageKey == null
				|| !storageKey.matches("(?i)[0-9a-f]{8}-(?:[0-9a-f]{4}-){3}[0-9a-f]{12}\\.pdf")) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV introuvable");
		}
		Path file = uploadDirectory.resolve(storageKey).normalize();
		if (!file.getParent().equals(uploadDirectory)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CV introuvable");
		}
		return file;
	}

	private String sanitizeFilename(String filename) {
		if (filename == null || filename.isBlank()) {
			return null;
		}
		String baseName = filename.replace('\\', '/');
		baseName = baseName.substring(baseName.lastIndexOf('/') + 1).replaceAll("[\\r\\n\"]", "_").trim();
		if (baseName.isBlank() || baseName.length() > 255) {
			return null;
		}
		return baseName;
	}

	public record StoredCv(String storageKey, String originalFilename) { }
}
