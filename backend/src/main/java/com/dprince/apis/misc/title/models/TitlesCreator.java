package com.dprince.apis.misc.title.models;

import com.dprince.entities.PersonTitle;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
public class TitlesCreator {
    @Valid
    @Size(min = 1, message="You should provide at least one title.")
    private List<PersonTitle> titles;

    @JsonIgnore
    private boolean update;

    // Adds a new title
    public void addTitle(PersonTitle title){
        if(this.getTitles()==null) this.titles = new ArrayList<>();
        this.titles.add(title);
    }
}
