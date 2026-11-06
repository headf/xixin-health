package com.xixin.health.common;

import com.xixin.health.appointment.Appointment;
import com.xixin.health.appointment.AppointmentRepository;
import com.xixin.health.appointment.AppointmentStatus;
import com.xixin.health.configuration.Branch;
import com.xixin.health.configuration.BranchRepository;
import com.xixin.health.configuration.Department;
import com.xixin.health.configuration.DepartmentRepository;
import com.xixin.health.configuration.ExamItem;
import com.xixin.health.configuration.ExamItemRepository;
import com.xixin.health.configuration.ExamPackage;
import com.xixin.health.configuration.ExamPackageRepository;
import com.xixin.health.configuration.ExamPackageItem;
import com.xixin.health.configuration.ExamPackageItemRepository;
import com.xixin.health.iam.Role;
import com.xixin.health.iam.RoleRepository;
import com.xixin.health.iam.UserAccount;
import com.xixin.health.iam.UserAccountRepository;
import com.xixin.health.order.Order;
import com.xixin.health.order.OrderRepository;
import com.xixin.health.order.OrderStatus;
import com.xixin.health.order.PaymentRecord;
import com.xixin.health.order.PaymentRecordRepository;
import com.xixin.health.order.PaymentStatus;
import com.xixin.health.profile.HealthProfile;
import com.xixin.health.profile.HealthProfileRepository;
import com.xixin.health.profile.ProfileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@ConditionalOnProperty(name = "xixin.data-seeder.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final HealthProfileRepository healthProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final OrderRepository orderRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final BranchRepository branchRepository;
    private final ExamPackageRepository examPackageRepository;
    private final DepartmentRepository departmentRepository;
    private final ExamItemRepository examItemRepository;
    private final ExamPackageItemRepository examPackageItemRepository;

    public DataSeeder(UserAccountRepository userAccountRepository,
                      RoleRepository roleRepository,
                      PasswordEncoder passwordEncoder,
                      HealthProfileRepository healthProfileRepository,
                      AppointmentRepository appointmentRepository,
                      OrderRepository orderRepository,
                      PaymentRecordRepository paymentRecordRepository,
                      BranchRepository branchRepository,
                      ExamPackageRepository examPackageRepository,
                      DepartmentRepository departmentRepository,
                      ExamItemRepository examItemRepository,
                      ExamPackageItemRepository examPackageItemRepository) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.healthProfileRepository = healthProfileRepository;
        this.appointmentRepository = appointmentRepository;
        this.orderRepository = orderRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.branchRepository = branchRepository;
        this.examPackageRepository = examPackageRepository;
        this.departmentRepository = departmentRepository;
        this.examItemRepository = examItemRepository;
        this.examPackageItemRepository = examPackageItemRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Ensure roles exist before seeding accounts
        ensureRole("NURSE", "护士", "STAFF");
        ensureRole("EXAM_DOCTOR", "体检医生", "STAFF");
        ensureRole("REVIEW_DOCTOR", "审核医生", "STAFF");
        ensureRole("NORMAL_USER", "普通用户", "USER");
        ensureRole("SYSTEM_ADMIN", "系统管理员", "ADMIN");
        ensureRole("OPERATIONS_ADMIN", "运营管理员", "ADMIN");

        // ========== 基础数据：分院、科室、检查项目、套餐 ==========
        seedBranch();
        seedDepartments();
        seedExamItems();
        seedPackages();

        // ========== 员工账户 ==========
        // 分院1：总院 (branchId=1)
        seedStaff("13800000100", "护士小王（总院）", "NURSE", 1L);
        seedStaff("13800001100", "护士小赵（总院）", "NURSE", 1L);
        seedStaff("13800000200", "体检医生老李（总院）", "EXAM_DOCTOR", 1L);
        seedStaff("13800000300", "审核医生张主任（总院）", "REVIEW_DOCTOR", 1L);
        // 分院2：浦东分院 (branchId=2)
        seedStaff("13800001200", "护士小陈（浦东）", "NURSE", 2L);
        seedStaff("13800001300", "体检医生老刘（浦东）", "EXAM_DOCTOR", 2L);
        seedStaff("13800001400", "审核医生王主任（浦东）", "REVIEW_DOCTOR", 2L);
        // 分院3：南山分院 (branchId=3)
        seedStaff("13800001500", "护士小李（南山）", "NURSE", 3L);
        seedStaff("13800001600", "体检医生老赵（南山）", "EXAM_DOCTOR", 3L);
        seedStaff("13800001700", "审核医生陈主任（南山）", "REVIEW_DOCTOR", 3L);

        // ========== 患者 & 演示预约数据 ==========
        // package 1=基础, 2=套餐一(全面型), 3=套餐二(精简型), 4=套餐三(深度型)
        // 身份证号由 IdCardGenerator 按真实规则生成（地区码+出生日期+校验码，第17位奇男偶女）
        seedPatient("13800000001", "张三", IdCardGenerator.generate("19900101", true), 2L);   // 男 1990年
        seedPatient("13800000002", "李四", IdCardGenerator.generate("19950515", false), 3L); // 女 1995年
        seedPatient("13800000003", "王五", IdCardGenerator.generate("19821008", false), 4L); // 女 1982年
        seedPatient("13800000004", "赵六", IdCardGenerator.generate("20000320", true), 1L);  // 男 2000年
        seedPatient("13800000005", "孙七", IdCardGenerator.generate("19881225", true), 2L);  // 男 1988年

        log.info("Demo accounts seeded (or already exist)");
    }

    // ========== 基础数据种子方法 ==========

    private void seedBranch() {
        if (branchRepository.findByEnabledTrue().isEmpty()) {
            branchRepository.save(Branch.create("BR001", "熙心健康体检中心（总院）", "北京市朝阳区建国路88号", "010-88886666", true));
            log.info("Created default branch: 熙心健康体检中心（总院）");
            branchRepository.save(Branch.create("BR002", "熙心健康体检中心（浦东分院）", "上海市浦东新区世纪大道100号", "021-68886666", true));
            log.info("Created default branch: 熙心健康体检中心（浦东分院）");
            branchRepository.save(Branch.create("BR003", "熙心健康体检中心（南山分院）", "深圳市南山区科技园南路12号", "0755-88886666", true));
            log.info("Created default branch: 熙心健康体检中心（南山分院）");
        }
    }

    private void seedDepartments() {
        if (departmentRepository.count() == 0) {
            Branch branch = branchRepository.findByEnabledTrue().stream().findFirst().orElse(null);
            if (branch == null) return;
            departmentRepository.save(Department.create(branch.getId(), "D001", "检验科", "2F-Area A", 30, true, true));
            departmentRepository.save(Department.create(branch.getId(), "D002", "影像科", "3F-Area B", 20, false, true));
            log.info("Created default departments: 检验科, 影像科");
        }
    }

    private void seedExamItems() {
        if (examItemRepository.count() == 0) {
            List<Department> depts = departmentRepository.findAll();
            Long labDeptId = depts.stream().filter(d -> "D001".equals(d.getDepartmentCode())).findFirst().map(Department::getId).orElse(null);
            Long imagingDeptId = depts.stream().filter(d -> "D002".equals(d.getDepartmentCode())).findFirst().map(Department::getId).orElse(null);

            // 检验科项目
            examItemRepository.save(ExamItem.create("ITEM_GLU", "血糖", labDeptId, "mmol/L",
                BigDecimal.valueOf(3.9), BigDecimal.valueOf(6.1), null, BigDecimal.valueOf(11.1), true));
            examItemRepository.save(ExamItem.create("ITEM_ALT", "谷丙转氨酶", labDeptId, "U/L",
                BigDecimal.valueOf(0), BigDecimal.valueOf(40), null, BigDecimal.valueOf(400), true));
            examItemRepository.save(ExamItem.create("ITEM_BLOOD_RT", "血常规", labDeptId, null, null, null, null, null, true));
            examItemRepository.save(ExamItem.create("ITEM_URINE_RT", "尿常规", labDeptId, null, null, null, null, null, true));
            examItemRepository.save(ExamItem.create("ITEM_LIPID", "血脂", labDeptId, "mmol/L", null, null, null, null, true));
            examItemRepository.save(ExamItem.create("ITEM_CR", "肌酐", labDeptId, "μmol/L",
                BigDecimal.valueOf(44), BigDecimal.valueOf(133), null, BigDecimal.valueOf(400), true));
            examItemRepository.save(ExamItem.create("ITEM_BUN", "尿素氮", labDeptId, "mmol/L",
                BigDecimal.valueOf(2.9), BigDecimal.valueOf(8.2), null, null, true));

            // 影像科项目
            examItemRepository.save(ExamItem.create("ITEM_CXR", "胸部X光", imagingDeptId, null, null, null, null, null, true));

            log.info("Created default exam items with Chinese names");
        }
    }

    private void seedPackages() {
        if (examPackageRepository.count() == 0) {
            // 基础套餐
            ExamPackage basic = ExamPackage.create("PKG_BASIC", "基础健康检查", "BASIC", "ALL", "18-65", 29900, true);
            basic = examPackageRepository.save(basic);
            // 套餐一（基础全面型）
            ExamPackage plus = ExamPackage.create("PKG_BASIC_PLUS", "套餐一（基础全面型）", "BASIC", "ALL", "18-65", 49900, true);
            plus = examPackageRepository.save(plus);
            // 套餐二（基础精简型）
            ExamPackage lite = ExamPackage.create("PKG_LITE", "套餐二（基础精简型）", "LITE", "ALL", "18-65", 19900, true);
            lite = examPackageRepository.save(lite);
            // 套餐三（深度体检型）
            ExamPackage full = ExamPackage.create("PKG_FULL", "套餐三（深度体检型）", "FULL", "ALL", "18-65", 99900, true);
            full = examPackageRepository.save(full);

            // 为每个套餐关联检查项目
            List<ExamItem> allItems = examItemRepository.findAll();

            // 基础套餐：全部项目
            int sort = 1;
            for (ExamItem item : allItems) {
                examPackageItemRepository.save(ExamPackageItem.of(basic.getId(), item.getId(), sort++));
            }
            // 套餐一：全部 + 肌酐 + 尿素氮
            sort = 1;
            for (ExamItem item : allItems) {
                if ("ITEM_CR".equals(item.getItemCode()) || "ITEM_BUN".equals(item.getItemCode())
                    || "ITEM_LIPID".equals(item.getItemCode())) continue;
                examPackageItemRepository.save(ExamPackageItem.of(plus.getId(), item.getId(), sort++));
            }
            // 套餐二：血糖 + 谷丙转氨酶 + 胸部X光
            sort = 1;
            for (ExamItem item : allItems) {
                if ("ITEM_GLU".equals(item.getItemCode()) || "ITEM_ALT".equals(item.getItemCode()) || "ITEM_CXR".equals(item.getItemCode())) {
                    examPackageItemRepository.save(ExamPackageItem.of(lite.getId(), item.getId(), sort++));
                }
            }
            // 套餐三：全部项目
            sort = 1;
            for (ExamItem item : allItems) {
                examPackageItemRepository.save(ExamPackageItem.of(full.getId(), item.getId(), sort++));
            }

            log.info("Created default packages with exam item associations");
        }
    }

    private void seedPatient(String phone, String displayName, String idCard, Long packageId) {
        boolean isNew = false;
        UserAccount account = userAccountRepository.findByPhone(phone).orElse(null);
        if (account == null) {
            Role normalUser = roleRepository.findByCode("NORMAL_USER").orElse(null);
            if (normalUser == null) {
                log.warn("NORMAL_USER role not found, skipping patient {}", phone);
                return;
            }
            account = new UserAccount(phone, null, displayName, passwordEncoder.encode("Passw0rd!"));
            // 设置身份证号（用于报告、就诊人信息等）
            account.setIdCard(idCard);
            // 根据身份证号设置性别（第17位奇数为男、偶数为女）
            String gender = IdCardGenerator.getGender(idCard);
            account.setGender(gender);
            account.addRole(normalUser);
            account = userAccountRepository.save(account);
            isNew = true;
            log.info("Seeded patient {}: {} / Passw0rd! (身份证: {}, 性别: {})",
                phone, displayName, idCard, gender);
        }

        if (!isNew) {
            // 如果已有预约记录则跳过（避免重复创建）
            boolean hasAppointment = appointmentRepository.countByAppointmentDate(LocalDate.now().plusDays(1)) > 0
                || appointmentRepository.countByAppointmentDate(LocalDate.now()) > 0;
            if (hasAppointment) return;
        }

        // 创建/更新就诊人信息（HealthProfile 中的身份证号、姓名）
        String gender = IdCardGenerator.getGender(idCard);
        String birthDate = IdCardGenerator.getBirthDate(idCard);
        HealthProfile existingProfile = healthProfileRepository
            .findByOwnerUserIdAndProfileTypeAndDeletedFalse(account.getId(), ProfileType.SELF).orElse(null);
        if (existingProfile == null) {
            HealthProfile profile = new HealthProfile(account.getId(), ProfileType.SELF);
            profile.update(displayName, idCard, phone, "无", "无", "紧急联系人", phone, ProfileType.SELF);
            healthProfileRepository.save(profile);
            log.info("Created health profile for {} (性别: {}, 生日: {})", displayName, gender, birthDate);
        } else {
            // 确保信息最新
            existingProfile.update(displayName, idCard, phone, "无", "无", "紧急联系人", phone, ProfileType.SELF);
            healthProfileRepository.save(existingProfile);
        }

        // Create appointments for today only
        LocalDate today = LocalDate.now();

        HealthProfile profile = healthProfileRepository
            .findByOwnerUserIdAndProfileTypeAndDeletedFalse(account.getId(), ProfileType.SELF)
            .orElse(null);

        if (profile == null) return;

        // Look up first enabled branch
        Branch branch = branchRepository.findByEnabledTrue().stream().findFirst().orElse(null);
        if (branch == null) {
            log.warn("No enabled branch found, skipping appointment/order for {}", displayName);
            return;
        }

        // Look up package price
        ExamPackage pkg = examPackageRepository.findById(packageId).orElse(null);
        int amountCent = (pkg != null) ? pkg.getPriceCent() : 29900;

        for (LocalDate date : List.of(today)) {
            Appointment apt = new Appointment(account.getId(), profile.getId(), packageId, branch.getId(), date, "08:00-09:00");
            apt.markPaid();
            appointmentRepository.save(apt);
            log.info("Created PAID appointment for {} on {} (branch={}, amount={})", displayName, date, branch.getBranchName(), amountCent);

            // Create order for this appointment
            Order order = new Order(apt.getId(), account.getId(), amountCent);
            order.markPaid();
            orderRepository.save(order);

            // Create payment record
            PaymentRecord payment = new PaymentRecord(order.getId(), "PAY-SEED-" + account.getId() + "-" + date.toString(), "SYSTEM", amountCent, PaymentStatus.SUCCESS);
            paymentRecordRepository.save(payment);
        }
    }

    private void ensureRole(String code, String name, String roleType) {
        roleRepository.findByCode(code).orElseGet(() -> {
            Role role = Role.system(code, name, roleType);
            log.info("Created role: {} ({})", code, name);
            return roleRepository.save(role);
        });
    }

    private void seedStaff(String phone, String displayName, String roleCode) {
        seedStaff(phone, displayName, roleCode, null);
    }

    private void seedStaff(String phone, String displayName, String roleCode, Long branchId) {
        if (userAccountRepository.findByPhone(phone).isPresent()) {
            return;
        }
        Role role = roleRepository.findByCode(roleCode).orElse(null);
        if (role == null) {
            log.warn("Role {} not found, skipping seed for {}", roleCode, phone);
            return;
        }
        UserAccount account = new UserAccount(phone, null, displayName, passwordEncoder.encode("Passw0rd!"));
        account.addRole(role);
        if (branchId != null) {
            account.setBranchId(branchId);
        }
        userAccountRepository.save(account);
        log.info("Seeded {} account: {} / Passw0rd!" + (branchId != null ? " (branchId=" + branchId + ")" : ""), roleCode, phone);
    }
}
