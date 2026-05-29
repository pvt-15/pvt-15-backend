package com.example.accessingdatamysql.auth.controller;

import com.example.accessingdatamysql.auth.dto.ForgotPasswordRequest;
import com.example.accessingdatamysql.auth.dto.ResetPasswordRequest;
import com.example.accessingdatamysql.auth.service.PasswordResetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints for the "forgot password" flow.
 *
 * <ul>
 *     <li>{@code POST /auth/forgot-password} – request a reset link by email.</li>
 *     <li>{@code GET  /auth/reset-password}  – the page the emailed link opens.</li>
 *     <li>{@code POST /auth/reset-password}  – submit the new password.</li>
 * </ul>
 *
 * <p>All three are public (no JWT), and are whitelisted in
 * {@code SecurityConfig}.</p>
 */
@RestController
@RequestMapping("/auth")
public class PasswordResetController {

    private static final String GENERIC_OK =
            "If an account exists for that email, a reset link has been sent.";

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    /**
     * Requests a password reset email.
     *
     * <p>Returns 200 with a generic message whether or not the email is
     * registered, to avoid leaking which emails have accounts. Returns 409 only
     * when the email belongs to a Google account, which cannot be reset.</p>
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            String email = request == null ? null : request.getEmail();
            passwordResetService.requestReset(email);
            return ResponseEntity.ok(GENERIC_OK);
        } catch (IllegalArgumentException e) {
            // "Email is required" -> 400, Google-account refusal -> 409.
            if ("Email is required".equals(e.getMessage())) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * Submits a new password using the token from the emailed link.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            String token = request == null ? null : request.getToken();
            String newPassword = request == null ? null : request.getNewPassword();
            passwordResetService.resetPassword(token, newPassword);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Serves the reset page that the emailed link points to. The page collects
     * a new password and submits it to {@code POST /auth/reset-password}.
     */
    @GetMapping(value = "/reset-password", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> resetPage(@RequestParam(value = "token", required = false) String token) {
        return ResponseEntity.ok(buildResetPage(token == null ? "" : token));
    }

    /**
     * Builds the self-contained HTML reset page. Kept inline (no template
     * engine) to avoid adding a view-layer dependency to this resource server.
     * The token is URL-safe base64, so it is safe to embed in a JS string.
     */
    private String buildResetPage(String token) {
        return "<!DOCTYPE html>\n"
                + "<html lang=\"sv\">\n"
                + "<head>\n"
                + "<meta charset=\"utf-8\">\n"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
                + "<title>Återställ lösenord – Skogsjakten</title>\n"
                + "<style>\n"
                + "  body{font-family:system-ui,-apple-system,Segoe UI,Roboto,sans-serif;"
                + "background:#BEDBB2;margin:0;display:flex;min-height:100vh;align-items:center;"
                + "justify-content:center;}\n"
                + "  .card{background:#fff;padding:32px;border-radius:16px;max-width:360px;width:90%;"
                + "box-shadow:0 8px 24px rgba(0,0,0,.12);}\n"
                + "  h1{font-size:20px;margin:0 0 16px;color:#2f4f2f;}\n"
                + "  label{display:block;font-size:14px;margin:12px 0 4px;color:#333;}\n"
                + "  input{width:100%;padding:10px;border:1px solid #ccc;border-radius:8px;"
                + "box-sizing:border-box;font-size:15px;}\n"
                + "  button{margin-top:20px;width:100%;padding:12px;border:0;border-radius:8px;"
                + "background:#4f8a4f;color:#fff;font-size:16px;cursor:pointer;}\n"
                + "  button:disabled{opacity:.6;cursor:default;}\n"
                + "  .msg{margin-top:16px;font-size:14px;}\n"
                + "  .err{color:#b00020;}\n"
                + "  .ok{color:#2f6f2f;}\n"
                + "  .hint{font-size:12px;color:#666;margin-top:4px;}\n"
                + "</style>\n"
                + "</head>\n"
                + "<body>\n"
                + "<div class=\"card\">\n"
                + "  <h1>Välj ett nytt lösenord</h1>\n"
                + "  <label for=\"pw\">Nytt lösenord</label>\n"
                + "  <input id=\"pw\" type=\"password\" autocomplete=\"new-password\">\n"
                + "  <div class=\"hint\">Minst 10 tecken, en stor bokstav och en siffra.</div>\n"
                + "  <label for=\"pw2\">Bekräfta lösenord</label>\n"
                + "  <input id=\"pw2\" type=\"password\" autocomplete=\"new-password\">\n"
                + "  <button id=\"submit\">Spara nytt lösenord</button>\n"
                + "  <div id=\"msg\" class=\"msg\"></div>\n"
                + "</div>\n"
                + "<script>\n"
                + "  var token = \"" + token + "\";\n"
                + "  var btn = document.getElementById('submit');\n"
                + "  var msg = document.getElementById('msg');\n"
                + "  btn.addEventListener('click', function(){\n"
                + "    var pw = document.getElementById('pw').value;\n"
                + "    var pw2 = document.getElementById('pw2').value;\n"
                + "    msg.className = 'msg';\n"
                + "    if(pw !== pw2){ msg.className='msg err'; msg.textContent='Lösenorden matchar inte.'; return; }\n"
                + "    btn.disabled = true;\n"
                + "    fetch('reset-password', {\n"
                + "      method:'POST',\n"
                + "      headers:{'Content-Type':'application/json'},\n"
                + "      body: JSON.stringify({token: token, newPassword: pw})\n"
                + "    }).then(function(r){\n"
                + "      if(r.status === 204){\n"
                + "        msg.className='msg ok';\n"
                + "        msg.textContent='Klart! Ditt lösenord har ändrats. Du kan nu logga in i appen.';\n"
                + "        btn.style.display='none';\n"
                + "      } else {\n"
                + "        return r.text().then(function(t){\n"
                + "          msg.className='msg err';\n"
                + "          msg.textContent = t || 'Något gick fel. Länken kan ha gått ut.';\n"
                + "          btn.disabled = false;\n"
                + "        });\n"
                + "      }\n"
                + "    }).catch(function(){\n"
                + "      msg.className='msg err'; msg.textContent='Nätverksfel. Försök igen.';\n"
                + "      btn.disabled = false;\n"
                + "    });\n"
                + "  });\n"
                + "</script>\n"
                + "</body>\n"
                + "</html>";
    }
}