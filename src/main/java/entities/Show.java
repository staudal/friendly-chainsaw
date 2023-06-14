package entities;

import dtos.ShowDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(name = "shows")
@NamedQuery(name = "Show.deleteAllRows", query = "DELETE from Show")
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "duration")
    private int duration;

    @Column(name = "date")
    private LocalDate date;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "festival")
    private Festival festival;

    @JoinTable(
            name = "shows_users",
            joinColumns = @JoinColumn(name = "show_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @ManyToMany(cascade = CascadeType.DETACH)
    private List<User> guests = new ArrayList<>();

    public Show(String name, int duration, LocalDate date) {
        this.name = name;
        this.duration = duration;
        this.date = date;
    }

    public Show(ShowDTO showDTO) {
        this.name = showDTO.getName();
        this.duration = showDTO.getDuration();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.date = LocalDate.parse(showDTO.getDate(), formatter);
    }
}
