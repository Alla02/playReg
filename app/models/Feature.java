package models;

import java.util.*;
import javax.persistence.*;

import io.ebean.*;
import play.data.format.*;
import play.data.validation.*;

@Entity
public class Feature extends Model {

    @Id
    public Long id;

    @Constraints.Required(message = "Обязательное поле")
    public String title;

    @Constraints.Required(message = "Обязательное поле")
    public String description;

    public String imageUrl;

    public static final Finder<Long, Feature> find = new Finder<>(Feature.class);
}