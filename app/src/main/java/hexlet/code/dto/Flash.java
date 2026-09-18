package hexlet.code.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public final class Flash {
    private String message;
    private int status;

    public void setFlash(String flashMessage, int statusCode) {
        message = flashMessage;
        status = statusCode;
    }

    public boolean isSucceeded() {
        return (status > 0);
    }

    public boolean isFail() {
        return (status < 0);
    }

    public boolean isActive() {
        return (status != 0);
    }
}
