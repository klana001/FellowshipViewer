package fellowship;

import com.moogiesoft.Viewer.ImagesLoader;
import com.moogiesoft.Viewer.PlayerViewerShim;
import com.moogiesoft.Viewer.Viewer;
import com.nmerrill.kothcomm.communication.Arguments;
import com.nmerrill.kothcomm.communication.Downloader;
import com.nmerrill.kothcomm.communication.LanguageLoader;
import com.nmerrill.kothcomm.communication.languages.java.JavaLoader;
import com.nmerrill.kothcomm.communication.languages.local.LocalJavaLoader;
import com.nmerrill.kothcomm.game.players.Submission;
//import com.nmerrill.kothcomm.game.runners.FixedCountRunner;
import com.nmerrill.kothcomm.game.scoring.Scoreboard;
import com.nmerrill.kothcomm.game.scoring.ScoredRankingsAggregator;
import com.nmerrill.kothcomm.game.tournaments.RoundRobin;
import com.nmerrill.kothcomm.ui.gui.GamePane;
import com.nmerrill.kothcomm.ui.gui.GraphMap2DView;
import com.nmerrill.kothcomm.ui.gui.TournamentPane;
import com.nmerrill.kothcomm.ui.text.TableBuilder;

import fellowship.actions.ReadonlyAction;
import fellowship.characters.BaseCharacter;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

import java.util.Random;

public class Main extends Application {

//	ReadonlyAction readonlyActionShim= new ReadonlyAction(null);
	
    private static com.nmerrill.kothcomm.game.TournamentRunner<Player, Fellowship> runner;
    
    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        TournamentPane<Player, Fellowship> runnerPane = new TournamentPane<>(runner, game -> {
            GamePane pane = new GamePane(game);
            GraphMap2DView<MapObject> mapView = new GraphMap2DView<>(game.getMap());
            mapView.addListener(mapObject -> {
                if (mapObject == null){
                    pane.setRight(null);
                } else if (mapObject instanceof BaseCharacter) {
                    BaseCharacter object = (BaseCharacter) mapObject;
                    pane.setRight(new CharacterView(object));
                }
            });
            pane.setCenter(mapView);
            return pane;
        });
        root.getChildren().add(runnerPane);
        root.setVisible(true);
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Fellowship");
        primaryStage.setHeight(500);
        primaryStage.setWidth(800);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Arguments arguments = Arguments.parse(args);
        LanguageLoader<Player> loader = new LanguageLoader<>(arguments);
//        loader.addLoader(new JavaLoader<>(Player.class));
//
//        if (arguments.validQuestionID()) {
//            new Downloader(loader, arguments.questionID).downloadQuestions();
//        }
//        Viewer viewer = new Viewer();
        
        LocalJavaLoader<Player> localLoader = new LocalJavaLoader<>(); 
//         Add your player here if you want to test it locally, and uncomment the next line
//        localLoader.register("CowardlySniperMk2",() -> new PlayerViewerShim(new CowardlySniperMk2(),ImagesLoader.loadCharacterImages("img/jael.png")));
//        localLoader.register("CowardlyWeakLightningSniper",() -> new PlayerViewerShim(new CowardlyWeakLightningSniper(),ImagesLoader.loadCharacterImages("img/jael.png")));
//        localLoader.register("DarkAbsorbers",() -> new PlayerViewerShim(new DarkAbsorbers(),ImagesLoader.loadCharacterImages("img/golbez.png")));
//        localLoader.register("LivingWall",() -> new PlayerViewerShim(new LivingWall(),ImagesLoader.loadCharacterImages("img/indianajones_whip.png")));

//        localLoader.register("QuickBot",() -> new PlayerViewerShim(new QuickBot(),ImagesLoader.loadCharacterImages("img/tremel.png")));
//        localLoader.register("CleaveBot",() -> new PlayerViewerShim(new CleaveBot(),ImagesLoader.loadCharacterImages("img/tremel.png")));
//        localLoader.register("WeaveBot",() -> new PlayerViewerShim(new WeaveBot(),ImagesLoader.loadCharacterImages("img/tremel.png")));
//        localLoader.register("TemplatePlayer",() -> new PlayerViewerShim(new TemplatePlayer(),ImagesLoader.loadCharacterImages("img/kavi.png")));
//        localLoader.register("Forester",() -> new PlayerViewerShim(new Forester(),ImagesLoader.loadCharacterImages("img/fey.png")));
//          localLoader.register("SniperSquad",() -> new PlayerViewerShim(new SniperSquad(),ImagesLoader.loadCharacterImages("img/redskull.png")));
//          localLoader.register("StaticCloud",() -> new PlayerViewerShim(new StaticCloud(),ImagesLoader.loadCharacterImages("img/silverbat.png")));

//        localLoader.register("Railbender", () -> new PlayerViewerShim(new Railbender(),ImagesLoader.loadCharacterImages("img/war.png")));
//        localLoader.register("Vampire", () -> new PlayerViewerShim(new Vampire(),ImagesLoader.loadCharacterImages("img/bloodelf_male1.png")));
        localLoader.register("LuckyDevil", () -> new PlayerViewerShim(new LuckyDevil(),ImagesLoader.loadCharacterImages("img/938123d0a346a4a7bb62fa7c1ee0.png")));

//        localLoader.register("Spiky",() -> new PlayerViewerShim(new Spiky(),ImagesLoader.loadCharacterImages("img/dart.png")));
//        localLoader.register("TemplatePlayer", TemplatePlayer::new);
//        localLoader.register("BearCavalry",() -> new PlayerViewerShim(new BearCavalry(),ImagesLoader.loadCharacterImages("img/schoolboy.png")));
//        localLoader.register("LongSword", () -> new PlayerViewerShim(new LongSword(),ImagesLoader.loadCharacterImages("img/egyptianqueen.png")));
//          localLoader.register("LongSwordv2", () -> new PlayerViewerShim(new LongSwordv2(),ImagesLoader.loadCharacterImages("img/gourrygabriev.png")));

        localLoader.register("TheWalkingDead", () -> new PlayerViewerShim(new TheWalkingDead(),ImagesLoader.loadCharacterImages("img/egyptianguy.png")));
//        localLoader.register("Invulnerables",() -> new PlayerViewerShim(new Invulnerables(),ImagesLoader.loadCharacterImages("img/mandar.png")));
//        localLoader.register("Forester", Forester::new);
//        localLoader.register("CowardlySniperMk2", CowardlySniperMk2::new);
//        localLoader.register("CowardlySniperMk1", CowardlySniperMk1::new);
//        localLoader.register("CowardlySniper2", CowardlySniper2::new);
//        localLoader.register("CowardlyWeakSmartSniper", CowardlyWeakSmartSniper::new);
//        localLoader.register("CowardlyWeakScoutSniper", CowardlyWeakScoutSniper::new);
//        localLoader.register("CowardlyWeakLightningSniper", CowardlyWeakLightningSniper::new);
//        localLoader.register("CowardlyWeakCleverSniper", CowardlyWeakCleverSniper::new);
//        localLoader.register("LightningBot", LightningBot::new);
//        localLoader.register("RogueSquad",() -> new PlayerViewerShim(new RogueSquad(),ImagesLoader.loadCharacterImages("img/darion.png")));
//        localLoader.register("LongSword", LongSword::new);
//        localLoader.register("Noob", () -> new PlayerViewerShim(new Noob(),ImagesLoader.loadCharacterImages("img/korea.png")));
//        localLoader.register("Knight", Knight::new);
//        localLoader.register("Knight",() -> new PlayerViewerShim(new Knight(),ImagesLoader.loadCharacterImages("img/dayita.png")));
//        localLoader.register("Wizard", Wizard::new);
//      local000Loader.register("Wizard",() -> new PlayerViewerShim(new Wizard(),ImagesLoader.loadCharacterImages("img/dayita.png")));
//        localLoader.register("Your player", YourPlayer::new);
        loader.addLoader(localLoader);
        MutableList<Submission<Player>> players = loader.load();
        
//        viewer.show();
//        viewer.setVisible(true);
        Random random = arguments.getRandom();
        runner = new com.nmerrill.kothcomm.game.TournamentRunner<>(new RoundRobin<>(players, random), new ScoredRankingsAggregator<>(), 2, Fellowship::new, random);

        if (arguments.useGui) {
            launch(Main.class);
        } else {
            //new FixedCountRunner<>(runner).run(arguments.iterations);
            
            System.out.println("Running "+arguments.iterations+" games");
            for (int i = 0; i < arguments.iterations; i++) {
//                printer.printProgress(i, arguments.iterations);
                runner.createGame().run();
            }
//            printer.printProgress(arguments.iterations, arguments.iterations);
            
            MutableList<Scoreboard<Submission<Player>>> scoreboards = runner.getScoreList();
            TableBuilder builder = new TableBuilder();
            builder.hasHeader(true);
            builder.setBorderType(TableBuilder.BorderType.ASCII);
            builder.rightAlign();
            MutableList<String> header = Lists.mutable.of("Name");
            players.sortThis();
            header.addAll(players.collect(Submission::getName));
            System.out.println(builder.display(players, p1 -> {
                MutableList<String> row = Lists.mutable.of(p1.getName());
                players.forEach(p2 -> {
                    if (p1.equals(p2)){
                        row.add("");
                        return;
                    }
                    int wins=0, ties=0, losses=0;
                    for (Scoreboard<Submission<Player>> scoreboard: scoreboards.select(s -> s.contains(p1) && s.contains(p2))){
                        int compare = scoreboard.compare(p1, p2);
                        if (compare < 0){
                            wins++;
                        } else if (compare > 0){
                            losses++;
                        } else {
                            ties++;
                        }
                    }
                    if (wins != 0 || ties != 0 || losses != 0) {
                        row.add(wins + "-" + ties + "-" + losses);
                    } else {
                        row.add("");
                    }
                });
                return row;
            }, header));

            System.exit(0);
        }
    }
}
