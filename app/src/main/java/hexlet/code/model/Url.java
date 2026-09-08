package hexlet.code.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Url {
    private Long id;
    private String name;
    private Instant createdAt;

    public Url(String name) {
        this.name = name;
    }
}
