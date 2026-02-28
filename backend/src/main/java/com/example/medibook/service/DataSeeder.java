package com.example.medibook.service;

import com.example.medibook.model.*;
import com.example.medibook.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

  private final AppUserRepository userRepo;
  private final SpecialtyRepository specialtyRepo;
  private final DoctorProfileRepository doctorRepo;
  private final WorkingHourRepository workingHourRepo;
  private final SlotService slotService;
  private final PasswordEncoder encoder;
  private final AppointmentRepository appointmentRepo;
  private final TimeSlotRepository timeSlotRepo;

  // Force redeploy to ensure admin seeding
  @Override
  public void run(ApplicationArguments args) {
    if (userRepo.count() == 0) {
        seedAll();
    } else {
        // Đảm bảo tnguyenanh189@gmail.com luôn tồn tại
        if (userRepo.findByEmail("tnguyenanh189@gmail.com").isEmpty()) {
            userRepo.save(AppUser.builder()
              .email("tnguyenanh189@gmail.com")
              .fullName("Official Admin")
              .passwordHash(encoder.encode("123"))
              .role(Role.ADMIN)
              .enabled(true)
              .build());
            System.out.println("==> Added missing admin: tnguyenanh189@gmail.com");
        }
        // Đảm bảo admin@gmail.com luôn tồn tại
        if (userRepo.findByEmail("admin@gmail.com").isEmpty()) {
            userRepo.save(AppUser.builder()
              .email("admin@gmail.com")
              .fullName("System Admin")
              .passwordHash(encoder.encode("123"))
              .role(Role.ADMIN)
              .enabled(true)
              .build());
            System.out.println("==> Added missing admin: admin@gmail.com");
        }
    }
  }

  /* ========== RECORD to hold each doctor's info ========== */
  record DocInfo(String name, String country, String specCode, String title,
                 String clinic, int yrsExp, long feeVnd, double rating, int ratingCount, String bio) {}

  private void seedAll() {
    /* ---------- 1) Specialties (12 chuyên khoa) ---------- */
    specialtyRepo.saveAll(List.of(
      Specialty.builder().code("CARDIO").name("Tim mạch").build(),
      Specialty.builder().code("DERMA").name("Da liễu").build(),
      Specialty.builder().code("DENTAL").name("Nha khoa").build(),
      Specialty.builder().code("ENT").name("Tai mũi họng").build(),
      Specialty.builder().code("BEAUTY").name("Thẩm mỹ").build(),
      Specialty.builder().code("NEURO").name("Thần kinh").build(),
      Specialty.builder().code("ORTHO").name("Chỉnh hình - Cơ xương khớp").build(),
      Specialty.builder().code("PEDIA").name("Nhi khoa").build(),
      Specialty.builder().code("GASTRO").name("Tiêu hóa").build(),
      Specialty.builder().code("OPHTHAL").name("Nhãn khoa").build(),
      Specialty.builder().code("OBGYN").name("Sản phụ khoa").build(),
      Specialty.builder().code("ONCOL").name("Ung bướu").build()
    ));

    // Map code -> Specialty entity for lookup
    Map<String, Specialty> specMap = new java.util.HashMap<>();
    specialtyRepo.findAll().forEach(s -> specMap.put(s.getCode(), s));

    /* ---------- 2) Admins ---------- */
    userRepo.save(AppUser.builder()
      .email("admin@gmail.com")
      .fullName("System Admin")
      .passwordHash(encoder.encode("123"))
      .role(Role.ADMIN)
      .enabled(true)
      .build());

    userRepo.save(AppUser.builder()
      .email("tnguyenanh189@gmail.com")
      .fullName("Official Admin")
      .passwordHash(encoder.encode("123"))
      .role(Role.ADMIN)
      .enabled(true)
      .build());

    /* ---------- 3) Doctor Profiles ---------- */
    List<DocInfo> doctors = List.of(
      // ===== TIM MẠCH (CARDIO) =====
      new DocInfo("Nguyễn Văn Minh", "Vietnam", "CARDIO", "PGS.TS.BS", "Bệnh viện Bạch Mai", 22, 450_000, 4.9, 312,
        "PGS.TS Nguyễn Văn Minh là chuyên gia hàng đầu về Tim mạch can thiệp, với hơn 22 năm kinh nghiệm tại Bệnh viện Bạch Mai. Chuyên điều trị bệnh mạch vành, suy tim, rối loạn nhịp tim."),
      new DocInfo("Trần Thị Hương", "Vietnam", "CARDIO", "TS.BS", "Bệnh viện Chợ Rẫy", 15, 400_000, 4.8, 198,
        "TS.BS Trần Thị Hương chuyên về Siêu âm tim và Tim mạch dự phòng. Tốt nghiệp chuyên khoa II tại Đại học Y Dược TP.HCM."),
      new DocInfo("David Miller", "USA", "CARDIO", "Dr.", "Mayo Clinic", 25, 1_500_000, 4.9, 520,
        "Dr. David Miller is a leading interventional cardiologist at Mayo Clinic with expertise in complex coronary interventions and structural heart disease."),

      // ===== DA LIỄU (DERMA) =====
      new DocInfo("Phạm Thị Trang", "Vietnam", "DERMA", "ThS.BS", "Bệnh viện Da liễu Trung ương", 12, 350_000, 4.7, 245,
        "ThS.BS Phạm Thị Trang chuyên điều trị các bệnh lý da liễu mạn tính, nấm da, viêm da cơ địa và các bệnh tự miễn qua da."),
      new DocInfo("Hans Müller", "Germany", "DERMA", "Dr.", "Charité Berlin", 18, 1_200_000, 4.8, 178,
        "Dr. Hans Müller là chuyên gia Da liễu tại Charité Berlin, chuyên về da liễu lâm sàng và nghiên cứu các phương pháp điều trị vảy nến, eczema tiên tiến."),

      // ===== NHA KHOA (DENTAL) =====
      new DocInfo("Lê Hoàng Nam", "Vietnam", "DENTAL", "BS.CKII", "Bệnh viện RHM Trung ương", 14, 300_000, 4.6, 167,
        "BS.CKII Lê Hoàng Nam chuyên về phẫu thuật hàm mặt, cấy ghép Implant và chỉnh nha cho người lớn và trẻ em."),
      new DocInfo("Tanaka Yuki", "Japan", "DENTAL", "Dr.", "Tokyo Dental College Hospital", 20, 1_300_000, 4.9, 290,
        "Dr. Tanaka Yuki là chuyên gia hàng đầu Nhật Bản về nha khoa thẩm mỹ và công nghệ phục hình răng sứ CAD/CAM."),

      // ===== TAI MŨI HỌNG (ENT) =====
      new DocInfo("Vũ Đức Hải", "Vietnam", "ENT", "PGS.TS.BS", "Bệnh viện Tai Mũi Họng TW", 20, 400_000, 4.8, 278,
        "PGS.TS Vũ Đức Hải chuyên phẫu thuật nội soi mũi xoang, vi phẫu thanh quản, và điều trị các bệnh lý tai mũi họng phức tạp."),
      new DocInfo("Kim Seo-jun", "Korea", "ENT", "Dr.", "Samsung Medical Center", 16, 1_100_000, 4.7, 156,
        "Dr. Kim Seo-jun chuyên về phẫu thuật tai giữa và cấy ốc tai điện tử, đã thực hiện hơn 2,000 ca phẫu thuật thành công."),

      // ===== THẨM MỸ (BEAUTY) =====
      new DocInfo("Hoàng Thị Linh", "Vietnam", "BEAUTY", "BS.CKI", "Bệnh viện JK Việt Nam", 10, 500_000, 4.7, 389,
        "BS Hoàng Thị Linh chuyên về căng da mặt, tiêm filler, botox và các phương pháp trẻ hóa da không xâm lấn."),
      new DocInfo("Park Ji-woo", "Korea", "BEAUTY", "Dr.", "Gangnam Severance Hospital", 15, 1_800_000, 4.9, 445,
        "Dr. Park Ji-woo là chuyên gia thẩm mỹ hàng đầu Hàn Quốc, nổi tiếng với kỹ thuật nâng mũi cấu trúc và tạo hình mắt tự nhiên."),

      // ===== THẦN KINH (NEURO) =====
      new DocInfo("Đặng Quốc Tuấn", "Vietnam", "NEURO", "GS.TS.BS", "Bệnh viện 108", 28, 500_000, 4.9, 410,
        "GS.TS Đặng Quốc Tuấn là chuyên gia đầu ngành về Thần kinh học, chuyên điều trị đột quỵ, Parkinson, Alzheimer và các bệnh thoái hóa thần kinh."),
      new DocInfo("Thomas Schneider", "Germany", "NEURO", "Prof. Dr.", "Universitätsklinikum Hamburg", 30, 2_000_000, 5.0, 380,
        "Prof. Dr. Thomas Schneider là giáo sư Thần kinh học tại ĐH Hamburg, chuyên về phẫu thuật thần kinh chức năng và điều trị động kinh kháng thuốc."),

      // ===== CHỈNH HÌNH (ORTHO) =====
      new DocInfo("Bùi Thanh Long", "Vietnam", "ORTHO", "TS.BS", "Bệnh viện Việt Đức", 17, 400_000, 4.8, 234,
        "TS.BS Bùi Thanh Long chuyên phẫu thuật thay khớp háng, khớp gối nhân tạo và nội soi khớp vai tại Bệnh viện Việt Đức."),
      new DocInfo("James Williams", "USA", "ORTHO", "Dr.", "Hospital for Special Surgery, NYC", 22, 1_600_000, 4.9, 467,
        "Dr. James Williams là bác sĩ phẫu thuật chỉnh hình thể thao tại HSS New York, chuyên gia tái tạo dây chằng chéo và phẫu thuật sụn chêm."),
      new DocInfo("Sato Hiroshi", "Japan", "ORTHO", "Dr.", "Osaka University Hospital", 19, 1_400_000, 4.8, 310,
        "Dr. Sato Hiroshi chuyên về phẫu thuật cột sống ít xâm lấn và điều trị thoát vị đĩa đệm bằng công nghệ laser tại ĐH Osaka."),

      // ===== NHI KHOA (PEDIA) =====
      new DocInfo("Ngô Thị Hoa", "Vietnam", "PEDIA", "PGS.TS.BS", "Bệnh viện Nhi Trung ương", 18, 350_000, 4.8, 520,
        "PGS.TS Ngô Thị Hoa là chuyên gia Nhi khoa hàng đầu, chuyên về hô hấp nhi, dị ứng miễn dịch trẻ em và dinh dưỡng nhi."),
      new DocInfo("Lim Wei Lin", "Singapore", "PEDIA", "Dr.", "KK Women's and Children's Hospital", 14, 1_000_000, 4.7, 198,
        "Dr. Lim Wei Lin chuyên về Nhi khoa tổng quát và Tim bẩm sinh ở trẻ em, đã điều trị thành công cho hơn 5,000 bệnh nhi."),

      // ===== TIÊU HÓA (GASTRO) =====
      new DocInfo("Phan Văn Đức", "Vietnam", "GASTRO", "TS.BS", "Bệnh viện Đại học Y Dược", 16, 400_000, 4.7, 267,
        "TS.BS Phan Văn Đức chuyên nội soi tiêu hóa can thiệp, điều trị viêm loét dạ dày, trào ngược và các bệnh lý gan mật."),
      new DocInfo("Robert Johnson", "USA", "GASTRO", "Dr.", "Cleveland Clinic", 20, 1_500_000, 4.8, 356,
        "Dr. Robert Johnson là chuyên gia Tiêu hóa tại Cleveland Clinic, chuyên về nội soi can thiệp, điều trị bệnh Crohn và viêm loét đại tràng."),

      // ===== NHÃN KHOA (OPHTHAL) =====
      new DocInfo("Dương Minh Anh", "Vietnam", "OPHTHAL", "BS.CKII", "Bệnh viện Mắt Trung ương", 15, 350_000, 4.8, 345,
        "BS.CKII Dương Minh Anh chuyên phẫu thuật Phaco, Lasik/Femto-Lasik, điều trị tật khúc xạ và bệnh lý võng mạc."),
      new DocInfo("Tan Cheng Wei", "Singapore", "OPHTHAL", "Dr.", "Singapore National Eye Centre", 18, 1_200_000, 4.9, 412,
        "Dr. Tan Cheng Wei là chuyên gia Nhãn khoa tại SNEC Singapore, chuyên về phẫu thuật đục thủy tinh thể và điều trị bệnh glaucoma tiên tiến."),

      // ===== SẢN PHỤ KHOA (OBGYN) =====
      new DocInfo("Lý Thị Quỳnh", "Vietnam", "OBGYN", "PGS.TS.BS", "Bệnh viện Từ Dũ", 20, 450_000, 4.9, 567,
        "PGS.TS Lý Thị Quỳnh là chuyên gia Sản phụ khoa tại BV Từ Dũ, chuyên về thai kỳ nguy cơ cao, IVF và phẫu thuật nội soi phụ khoa."),
      new DocInfo("Jean Martin", "France", "OBGYN", "Dr.", "Hôpital Necker, Paris", 22, 1_400_000, 4.8, 289,
        "Dr. Jean Martin là chuyên gia Sản phụ khoa tại BV Necker Paris, chuyên về y học bào thai và chẩn đoán trước sinh tiên tiến."),

      // ===== UNG BƯỚU (ONCOL) =====
      new DocInfo("Hồ Chí Dũng", "Vietnam", "ONCOL", "GS.TS.BS", "Bệnh viện K", 25, 500_000, 4.9, 478,
        "GS.TS Hồ Chí Dũng là chuyên gia Ung bướu hàng đầu Việt Nam, chuyên về ung thư phổi, ung thư vú và liệu pháp miễn dịch ung thư."),
      new DocInfo("Michael Brown", "Singapore", "ONCOL", "Dr.", "National Cancer Centre Singapore", 20, 1_800_000, 4.9, 356,
        "Dr. Michael Brown là chuyên gia Ung bướu tại NCCS Singapore, chuyên về xạ trị chính xác (IMRT/Proton) và liệu pháp nhắm đích phân tử.")
    );

    /* ---------- Seed each doctor ---------- */
    int idx = 1;
    ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
    for (DocInfo d : doctors) {
      Specialty spec = specMap.get(d.specCode());
      if (spec == null) continue;

      AppUser du = userRepo.save(AppUser.builder()
        .email(com.example.medibook.utils.StringNormalizationUtils.toEmailFormat(d.name()))
        .fullName(d.name())
        .passwordHash(encoder.encode("123"))
        .role(Role.DOCTOR)
        .enabled(true)
        .build());

      String avatarUrl = "https://randomuser.me/api/portraits/" + (idx % 2 == 0 ? "women" : "men") + "/" + (idx % 99) + ".jpg";

      DoctorProfile dp = doctorRepo.save(DoctorProfile.builder()
        .user(du)
        .specialty(spec)
        .title(d.title())
        .clinicName(d.clinic())
        .yearsExperience(d.yrsExp())
        .consultFeeVnd(d.feeVnd())
        .bio(d.bio())
        .avatarUrl(avatarUrl)
        .country(d.country())
        .rating(d.rating())
        .ratingCount(d.ratingCount())
        .build());

      // Working hours Mon-Sat
      for (short dow = 1; dow <= 6; dow++) {
        workingHourRepo.save(WorkingHour.builder()
          .doctor(dp)
          .dayOfWeek(dow)
          .startTime(LocalTime.of(8, 0))
          .endTime(LocalTime.of(17, 0))
          .slotMinutes((short) 30)
          .breakStart(LocalTime.of(12, 0))
          .breakEnd(LocalTime.of(13, 0))
          .build());
      }

      try {
        slotService.generateSlots(dp.getId(), LocalDate.now(zone), LocalDate.now(zone).plusDays(13), zone);
      } catch (Exception ignored) {}

      idx++;
    }

    /* ---------- 4) Patient Users ---------- */
    for (int i = 1; i <= 10; i++) {
      userRepo.save(AppUser.builder()
        .email("patient" + i + "@gmail.com")
        .fullName("Bệnh nhân " + i)
        .passwordHash(encoder.encode("123"))
        .role(Role.USER)
        .enabled(true)
        .build());
    }

    /* ---------- 5) Seed Some Appointments ---------- */
    List<AppUser> patients = userRepo.findAll().stream()
                .filter(u -> u.getRole() == Role.USER)
                .toList();
    List<DoctorProfile> seededDocs = doctorRepo.findAll();

    int appCount = 0;
    for (int i = 0; i < 5 && i < seededDocs.size(); i++) {
        DoctorProfile dp = seededDocs.get(i);
        List<TimeSlot> slots = timeSlotRepo.findByDoctorIdAndStartAtBetweenAndStatusOrderByStartAt(
                dp.getId(),
                LocalDate.now(zone).atStartOfDay(zone).toInstant(),
                LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant(),
                TimeSlotStatus.AVAILABLE
        );

        for (int j = 0; j < 3 && j < slots.size() && j < patients.size(); j++) {
            TimeSlot slot = slots.get(j);
            AppUser patient = patients.get(j);

            appointmentRepo.save(Appointment.builder()
                    .doctor(dp)
                    .patient(patient)
                    .timeSlot(slot)
                    .status(AppointmentStatus.BOOKED)
                    .patientNote("Lịch hẹn mẫu số " + (++appCount))
                    .build());

            slot.setStatus(TimeSlotStatus.BOOKED);
            timeSlotRepo.save(slot);
        }
    }

    System.out.println("======================================");
    System.out.println("  SEEDED " + doctors.size() + " DOCTORS, 12 SPECIALTIES, " + appCount + " APPOINTMENTS");
    System.out.println("======================================");
    System.out.println("ADMIN  : admin@medibook.local / Password@123");
    System.out.println("DOCTOR : doctor1@medibook.local / Password@123");
    System.out.println("USER   : user1@medibook.local / Password@123");
  }
}
