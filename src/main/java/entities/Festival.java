package entities;

import dtos.FestivalDTO;
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
@Table(name = "festivals")
public class Festival {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "city")
    private String city;

    @Column(name = "startDate")
    private LocalDate startDate;

    @Column(name = "endDate")
    private LocalDate endDate;

    @OneToMany(mappedBy = "festival")
    private List<User> guests = new ArrayList<>();

    public Festival(FestivalDTO festivalToAdd) {
        this.name = festivalToAdd.getName();
        this.city = festivalToAdd.getCity();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.startDate = LocalDate.parse(festivalToAdd.getStartDate(), formatter);
        this.endDate = LocalDate.parse(festivalToAdd.getEndDate(), formatter);
    }

    public Festival(String name, String city, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.city = city;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
