package com.noisevisionsoftware.nutrilog.service.tenant;

import com.google.cloud.Timestamp;
import com.noisevisionsoftware.nutrilog.model.TenantConfig;
import com.noisevisionsoftware.nutrilog.model.user.Gender;
import com.noisevisionsoftware.nutrilog.model.user.User;
import com.noisevisionsoftware.nutrilog.model.user.UserRole;
import com.noisevisionsoftware.nutrilog.repository.DietRepository;
import com.noisevisionsoftware.nutrilog.repository.UserRepository;
import com.noisevisionsoftware.nutrilog.repository.tenant.TenantConfigRepository;
import com.noisevisionsoftware.nutrilog.service.diet.DietManagerService;
import com.noisevisionsoftware.nutrilog.service.diet.DietService;
import com.noisevisionsoftware.nutrilog.service.firebase.FileStorageService;
import com.noisevisionsoftware.nutrilog.utils.excelParser.model.ParsedDietData;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Slf4j
public class DemoTenantService {
    private final TenantConfigRepository tenantConfigRepository;
    private final UserRepository userRepository;
    private final DietRepository dietRepository;
    private final DietManagerService dietManagerService;
    private final DietService dietService;
    private final ResourceLoader resourceLoader;
//    private final BCryptPasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Value("${app.demo.template-dir:classpath:demo-templates/}")
    private String templateDir;
//
//    public TenantConfig createDemoTenant(String name, String email) {
//        if (tenantConfigRepository.findByEmail(email).isPresent()) {
//            throw new IllegalArgumentException("Tenant o podanym emailu już istnieje");
//        }
//
//        String demoPassword = generateRandomPassword();
//
//        TenantConfig config = TenantConfig.builder()
//                .name(name)
//                .email(email)
//                .isDemoAccount(true)
//                .createdAt(com.google.cloud.Timestamp.now())
//                .expiresAt(com.google.cloud.Timestamp.ofTimeSecondsAndNanos(
//                        com.google.cloud.Timestamp.now().getSeconds() + (30 * 24 * 60 * 60), 0))
//                .demoPassword(passwordEncoder.encode(demoPassword))
//                .demoUsers(new ArrayList<>())
//                .demoDiets(new ArrayList<>())
//                .initialized(false)
//                .build();
//
//        TenantConfig savedConfig = tenantConfigRepository.save(config);
//        log.info("Created demo tenant: {} with plain password: {}", savedConfig.getId(), demoPassword);
//
//        return savedConfig;
//    }

//    public void initializeDemoData(String tenantId) {
//        TenantConfig config = tenantConfigRepository.findById(tenantId)
//                .orElseThrow(() -> new IllegalArgumentException("Tenant nie istnieje"));
//
//        if (config.isInitialized()) {
//            log.info("Tenant {} already initialized", tenantId);
//            return;
//        }
//
//        try {
//            // Tworzenie przykładowych użytkowników
//            List<String> demoUserIds = createDemoUsers(config, 5);
//            config.setDemoUsers(demoUserIds);
//
//            // Przygotowywanie przykładowej diety
//            List<String> demoDiet
//        }
//    }

    private List<String> createDemoUsers(TenantConfig config, int count) {
        List<String> userIds = new ArrayList<>();
        String[] maleNames = {"Adam", "Michał", "Piotr", "Jan", "Tomasz", "Krzysztof", "Paweł"};
        String[] femaleNames = {"Anna", "Maria", "Katarzyna", "Małgorzata", "Agnieszka", "Ewa", "Magdalena"};
        String[] lastNames = {"Nowak", "Kowalski", "Wiśniewski", "Wójcik", "Kowalczyk", "Kamiński", "Lewandowski"};

        for (int i = 0; i < count; i++) {
            boolean isMale = ThreadLocalRandom.current().nextBoolean();
            String[] names = isMale ? maleNames : femaleNames;
            String firstName = names[ThreadLocalRandom.current().nextInt(names.length)];
            String lastName = lastNames[ThreadLocalRandom.current().nextInt(lastNames.length)];

            if (!isMale && lastName.endsWith("i")) {
                lastName = lastName.substring(0, lastName.length() - 1) + "a";
            }

            int age = ThreadLocalRandom.current().nextInt(18, 66);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -age);

            User user = User.builder()
                    .email(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@demo" + config.getId() + ".pl")
                    .nickname(firstName + " " + lastName)
                    .gender(isMale ? Gender.MALE : Gender.FEMALE)
                    .birthDate(calendar.getTimeInMillis())
                    .storedAge(age)
                    .profileCompleted(true)
                    .role(UserRole.USER)
                    .note("Przykładowy użytkownik dla konta demo " + config.getName())
                    .createdAt(System.currentTimeMillis())
                    .build();

            User savedUser = userRepository.save(user);
            userIds.add(savedUser.getId());

        }

        return userIds;
    }
//
//    private List<String> createDemoDiets(TenantConfig config, List<String> userIds) throws IOException {
//        List<String> dietIds = new ArrayList<>();
//
//        Resource templateDir = resourceLoader.getResource(this.templateDir);
//        File dir = templateDir.getFile();
//
//        if (!dir.exists() || !dir.isDirectory()) {
//            log.error("Template directory not found: {}", this.templateDir);
//            throw new IOException("Template directory not found");
//        }
//
//        File[] templateFiles = dir.listFiles((d, name) -> name.endsWith(".xlxs"));
//
//        if (templateFiles == null || templateFiles.length == 0) {
//            log.error("No template files found in {}", this.templateDir);
//            throw new IOException("No template files found");
//        }
//
//        for (int i = 0; i < Math.min(userIds.size(), templateFiles.length); i++) {
//            String userId = userIds.get(i);
//            File templateFile = templateFiles[i];
//
//            Path tempFile = Files.createTempFile("diet-template-", ".xlsx");
//            Files.copy(templateFile.toPath(), tempFile, StandardCopyOption.REPLACE_EXISTING);
//
//            MultipartFile multipartFile = new MultipartFile() {
//                @Override
//                @NonNull
//                public String getName() {
//                    return templateFile.getName();
//                }
//
//                @Override
//                public String getOriginalFilename() {
//                    return templateFile.getName();
//                }
//
//                @Override
//                public String getContentType() {
//                    return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
//                }
//
//                @Override
//                public boolean isEmpty() {
//                    return false;
//                }
//
//                @Override
//                public long getSize() {
//                    return templateFile.length();
//                }
//
//                @Override
//                public byte[] getBytes() throws IOException {
//                    return Files.readAllBytes(tempFile);
//                }
//
//                @Override
//                @NonNull
//                public InputStream getInputStream() throws IOException {
//                    return Files.newInputStream(tempFile);
//                }
//
//                @Override
//                public void transferTo(File dest) throws IOException, IllegalStateException {
//                    Files.copy(tempFile, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
//                }
//            };
//
//            try {
//                String fileUrl = fileStorageService.uploadFile(multipartFile, userId);
//                String fileName = templateFile.getName();
//
//                ParsedDietData parsedData = dietManagerService.saveDietWithShoppingList(multipartFile);
//            }
//
//
//        }
//    }



    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            password.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        return password.toString();
    }
}