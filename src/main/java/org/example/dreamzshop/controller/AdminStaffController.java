package org.example.dreamzshop.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dreamzshop.dto.StaffRequest;
import org.example.dreamzshop.entity.User;
import org.example.dreamzshop.enums.Role;
import org.example.dreamzshop.repository.UserRepository;
import org.example.dreamzshop.service.StaffService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/staff")
@RequiredArgsConstructor
public class AdminStaffController {

    private final StaffService staffService;
    private final UserRepository userRepository;

    @GetMapping
    public String staffList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {

        int pageSize = 10;

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                pageSize,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Page<User> staffPage =
                staffService.getAllStaff(
                        keyword,
                        pageable
                );

        long totalStaff =
                userRepository.countByRole(Role.STAFF);

        long activeStaff =
                userRepository.countByRoleAndEnabledTrue(
                        Role.STAFF
                );

        long inactiveStaff =
                userRepository.countByRoleAndEnabledFalse(
                        Role.STAFF
                );

        model.addAttribute(
                "staffPage",
                staffPage
        );

        model.addAttribute(
                "staff",
                staffPage.getContent()
        );

        model.addAttribute(
                "keyword",
                keyword == null ? "" : keyword
        );

        model.addAttribute(
                "currentPage",
                staffPage.getNumber()
        );

        model.addAttribute(
                "totalPages",
                staffPage.getTotalPages()
        );

        model.addAttribute(
                "totalStaff",
                totalStaff
        );

        model.addAttribute(
                "activeStaff",
                activeStaff
        );

        model.addAttribute(
                "inactiveStaff",
                inactiveStaff
        );

        return "admin/staff";
    }

    @GetMapping("/add")
    public String addStaff(Model model) {

        model.addAttribute(
                "staffRequest",
                new StaffRequest()
        );

        model.addAttribute(
                "pageTitle",
                "Add Staff"
        );

        model.addAttribute(
                "formAction",
                "/admin/staff/save"
        );

        return "admin/staff-form";
    }

    @PostMapping("/save")
    public String saveStaff(
            @Valid @ModelAttribute("staffRequest")
            StaffRequest request,

            BindingResult bindingResult,

            RedirectAttributes redirectAttributes,

            Model model
    ) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "pageTitle",
                    "Add Staff"
            );

            model.addAttribute(
                    "formAction",
                    "/admin/staff/save"
            );

            return "admin/staff-form";
        }

        try {

            staffService.createStaff(
                    request.getFullName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getPassword()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Staff member created successfully"
            );

            return "redirect:/admin/staff";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            model.addAttribute(
                    "pageTitle",
                    "Add Staff"
            );

            model.addAttribute(
                    "formAction",
                    "/admin/staff/save"
            );

            return "admin/staff-form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editStaff(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        try {

            User staff =
                    staffService.getStaff(id);

            StaffRequest request =
                    StaffRequest.builder()
                            .fullName(staff.getFullName())
                            .email(staff.getEmail())
                            .phone(staff.getPhone())
                            .build();

            model.addAttribute(
                    "staffRequest",
                    request
            );

            model.addAttribute(
                    "staffId",
                    id
            );

            model.addAttribute(
                    "pageTitle",
                    "Edit Staff"
            );

            model.addAttribute(
                    "formAction",
                    "/admin/staff/update/" + id
            );

            return "admin/staff-form";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/admin/staff";
        }
    }

    @PostMapping("/update/{id}")
    public String updateStaff(
            @PathVariable Long id,

            @Valid @ModelAttribute("staffRequest")
            StaffRequest request,

            BindingResult bindingResult,

            RedirectAttributes redirectAttributes,

            Model model
    ) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "staffId",
                    id
            );

            model.addAttribute(
                    "pageTitle",
                    "Edit Staff"
            );

            model.addAttribute(
                    "formAction",
                    "/admin/staff/update/" + id
            );

            return "admin/staff-form";
        }

        try {

            staffService.updateStaff(
                    id,
                    request.getFullName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getPassword()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Staff member updated successfully"
            );

            return "redirect:/admin/staff";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            model.addAttribute(
                    "staffId",
                    id
            );

            model.addAttribute(
                    "pageTitle",
                    "Edit Staff"
            );

            model.addAttribute(
                    "formAction",
                    "/admin/staff/update/" + id
            );

            return "admin/staff-form";
        }
    }

    @PostMapping("/toggle/{id}")
    public String toggleStaff(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        try {

            staffService.toggleStaff(id);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Staff status updated successfully"
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/admin/staff";
    }
}