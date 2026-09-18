package hexlet.code.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public final class Flash {
    public static final int SUCCESS = 1;
    public static final int FAIL = -1;
    public static final int NOT_READY = -1;

    private String message;
    private int status;

    public void setFlash(String flashMessage, int statusCode) {
        message = flashMessage;

        if (statusCode > 0) {
            status = SUCCESS;
        } else if (statusCode < 0) {
            status = FAIL;
        } else {
            status = NOT_READY;
        }
    }

    public boolean isSucceeded() {
        return (status == SUCCESS);
    }

    public boolean isFail() {
        return (status == FAIL);
    }

    public boolean isActive() {
        return (status != NOT_READY);
    }
}
