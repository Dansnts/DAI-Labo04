package ch.heigvd.dai;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(8080);

        Map<String, Pokemon> pokedex = new HashMap<>();

        app.post("/pokemon", ctx -> {
            Pokemon newPokemon = ctx.bodyAsClass(Pokemon.class);
            if (pokedex.containsKey(newPokemon.getNumber())) {
                ctx.status(HttpStatus.CONFLICT).result("A Pokémon with this Pokédex number already exists.");
                return;
            }
            pokedex.put(newPokemon.getNumber(), newPokemon);
            ctx.status(HttpStatus.CREATED).json(newPokemon);
        });

        app.post("/pokemon/batch", ctx -> {
            List<Pokemon> newPokemons = ctx.bodyAsClass(ArrayList.class);
            List<Pokemon> addedPokemons = new ArrayList<>();

            for (Object obj : newPokemons) {
                Map<String, Object> map = (Map<String, Object>) obj;

                String number = (String) map.get("number");
                if (pokedex.containsKey(number)) {
                    continue; // Skip Pokémon with duplicate numbers
                }

                Pokemon pokemon = new Pokemon();
                pokemon.setNumber(number);
                pokemon.setName((String) map.get("name"));
                pokemon.setTypes((List<String>) map.get("types"));
                pokemon.setDescription((String) map.get("description"));
                pokemon.setSize((Double) map.get("size"));
                pokemon.setWeight((Double) map.get("weight"));
                pokemon.setGenderOptions((List<String>) map.get("genderOptions"));
                pokemon.setShinyLock((Boolean) map.get("shinyLock"));
                pokemon.setRegions((List<String>) map.get("regions"));

                pokedex.put(number, pokemon);
                addedPokemons.add(pokemon);
            }

            ctx.status(HttpStatus.CREATED).json(addedPokemons);
        });


        app.get("/pokemon/{number}", ctx -> {
            String number = ctx.pathParam("number");
            Pokemon pokemon = pokedex.get(number);
            if (pokemon != null) {
                ctx.status(HttpStatus.OK).json(pokemon);
            } else {
                ctx.status(HttpStatus.NOT_FOUND).result("Pokémon not found");
            }
        });

        app.patch("/pokemon/{number}", ctx -> {
            String number = ctx.pathParam("number");
            Pokemon existingPokemon = pokedex.get(number);
            if (existingPokemon != null) {
                Pokemon updatedData = ctx.bodyAsClass(Pokemon.class);
                if (updatedData.getName() != null) existingPokemon.setName(updatedData.getName());
                if (updatedData.getTypes() != null) existingPokemon.setTypes(updatedData.getTypes());
                if (updatedData.getDescription() != null) existingPokemon.setDescription(updatedData.getDescription());
                if (updatedData.getSize() != 0) existingPokemon.setSize(updatedData.getSize());
                if (updatedData.getWeight() != 0) existingPokemon.setWeight(updatedData.getWeight());
                if (updatedData.getGenderOptions() != null) existingPokemon.setGenderOptions(updatedData.getGenderOptions());
                existingPokemon.setShinyLock(updatedData.isShinyLock());
                if (updatedData.getRegions() != null) existingPokemon.setRegions(updatedData.getRegions());
                ctx.status(HttpStatus.OK).json(existingPokemon);
            } else {
                ctx.status(HttpStatus.NOT_FOUND).result("Pokémon not found");
            }
        });

        app.delete("/pokemon/{number}", ctx -> {
            String number = ctx.pathParam("number");
            if (pokedex.remove(number) != null) {
                ctx.status(HttpStatus.NO_CONTENT);
            } else {
                ctx.status(HttpStatus.NOT_FOUND).result("Pokémon not found");
            }
        });

        app.get("/pokemon", ctx -> ctx.status(HttpStatus.OK).json(pokedex.values()));

        // Endpoint pour générer une page HTML avec un tableau
        app.get("/pokemon-html", ctx -> {
            if (pokedex.isEmpty()) {
                ctx.status(HttpStatus.OK).result("No Pokémon found in the Pokédex.");
                return;
            }

            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<!DOCTYPE html>");
            htmlBuilder.append("<html lang='en'>");
            htmlBuilder.append("<head>");
            htmlBuilder.append("<meta charset='UTF-8'>");
            htmlBuilder.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            htmlBuilder.append("<title>Pokédex</title>");
            htmlBuilder.append("<style>");
            htmlBuilder.append("table { width: 100%; border-collapse: collapse; }");
            htmlBuilder.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            htmlBuilder.append("th { background-color: #f4f4f4; }");
            htmlBuilder.append("</style>");
            htmlBuilder.append("</head>");
            htmlBuilder.append("<body>");
            htmlBuilder.append("<h1>Pokédex</h1>");
            htmlBuilder.append("<table>");
            htmlBuilder.append("<thead>");
            htmlBuilder.append("<tr>");
            htmlBuilder.append("<th>Number</th>");
            htmlBuilder.append("<th>Name</th>");
            htmlBuilder.append("<th>Types</th>");
            htmlBuilder.append("<th>Description</th>");
            htmlBuilder.append("<th>Size</th>");
            htmlBuilder.append("<th>Weight</th>");
            htmlBuilder.append("<th>Gender Options</th>");
            htmlBuilder.append("<th>Shiny Lock</th>");
            htmlBuilder.append("<th>Regions</th>");
            htmlBuilder.append("</tr>");
            htmlBuilder.append("</thead>");
            htmlBuilder.append("<tbody>");

            for (Pokemon pokemon : pokedex.values()) {
                htmlBuilder.append("<tr>");
                htmlBuilder.append("<td>").append(pokemon.getNumber()).append("</td>");
                htmlBuilder.append("<td>").append(pokemon.getName()).append("</td>");
                htmlBuilder.append("<td>").append(String.join(", ", pokemon.getTypes())).append("</td>");
                htmlBuilder.append("<td>").append(pokemon.getDescription()).append("</td>");
                htmlBuilder.append("<td>").append(pokemon.getSize()).append(" m</td>");
                htmlBuilder.append("<td>").append(pokemon.getWeight()).append(" kg</td>");
                htmlBuilder.append("<td>").append(String.join(", ", pokemon.getGenderOptions())).append("</td>");
                htmlBuilder.append("<td>").append(pokemon.isShinyLock() ? "Yes" : "No").append("</td>");
                htmlBuilder.append("<td>").append(String.join(", ", pokemon.getRegions())).append("</td>");
                htmlBuilder.append("</tr>");
            }

            htmlBuilder.append("</tbody>");
            htmlBuilder.append("</table>");
            htmlBuilder.append("</body>");
            htmlBuilder.append("</html>");

            ctx.html(htmlBuilder.toString());
        });
    }

    public static class Pokemon {
        private String number; // Identifiant unique (numéro de Pokédex)
        private String name;
        private List<String> types;
        private String description;
        private double size;
        private double weight;
        private List<String> genderOptions; // Liste des options de genre
        private boolean shinyLock; // Indique si le Pokémon est verrouillé en shiny
        private List<String> regions;

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getTypes() {
            return types;
        }

        public void setTypes(List<String> types) {
            this.types = types;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public double getSize() {
            return size;
        }

        public void setSize(double size) {
            this.size = size;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public List<String> getGenderOptions() {
            return genderOptions;
        }

        public void setGenderOptions(List<String> genderOptions) {
            this.genderOptions = genderOptions;
        }

        public boolean isShinyLock() {
            return shinyLock;
        }

        public void setShinyLock(boolean shinyLock) {
            this.shinyLock = shinyLock;
        }

        public List<String> getRegions() {
            return regions;
        }

        public void setRegions(List<String> regions) {
            this.regions = regions;
        }
    }
}