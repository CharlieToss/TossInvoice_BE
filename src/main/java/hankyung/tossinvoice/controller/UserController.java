package hankyung.tossinvoice.controller;

import hankyung.tossinvoice.dto.user.res.CompanySearchResponse;
import hankyung.tossinvoice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/api/v1/company-search/{businessNumber}")
    public ResponseEntity<CompanySearchResponse> findByBusinessNumber(
            @PathVariable String businessNumber
    ) {
        CompanySearchResponse companySearchResponse = userService.findByBusinessNumber(businessNumber);

        return ResponseEntity.ok().body(companySearchResponse);
    }
}
