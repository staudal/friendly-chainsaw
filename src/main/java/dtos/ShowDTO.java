package dtos;

import entities.Show;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ShowDTO {
    private Long id;
    private String name;
    private int duration;
    private String date;
    private Long festival;

    public ShowDTO(String name, int duration, String date) {
        this.name = name;
        this.duration = duration;
        this.date = date;
    }

    public ShowDTO(Show show) {
        this.id = show.getId();
        this.name = show.getName();
        this.duration = show.getDuration();
        this.date = show.getDate().toString();
        if (show.getFestival() != null) {
            this.festival = show.getFestival().getId();
        }
    }

    public static List<ShowDTO> getShowDTOs(List<Show> shows) {
        List<ShowDTO> showDTOs = new ArrayList<>();
        shows.forEach(show -> showDTOs.add(new ShowDTO(show)));
        return showDTOs;
    }
}
