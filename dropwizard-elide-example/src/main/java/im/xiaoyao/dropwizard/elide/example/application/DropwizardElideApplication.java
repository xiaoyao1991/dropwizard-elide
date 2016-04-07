package im.xiaoyao.dropwizard.elide.example.application;

import com.yahoo.elide.resources.JsonApiEndpoint;
import im.xiaoyao.dropwizard.elide.ElideBundle;
import im.xiaoyao.dropwizard.elide.example.persistence.Author;
import im.xiaoyao.dropwizard.elide.example.persistence.Book;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Created by xiaoyaoqian on 4/6/16.
 */
public class DropwizardElideApplication extends Application<DropwizardElideConfiguration> {
    private final String name = "dropwizard-elide-example";

    private final ElideBundle<DropwizardElideConfiguration> elideBundle = new ElideBundle<DropwizardElideConfiguration>(
            Author.class,
            Book.class
    ) {
        @Override
        public DataSourceFactory getDataSourceFactory(DropwizardElideConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };

    @Override
    public void initialize(Bootstrap<DropwizardElideConfiguration> bootstrap) {
        bootstrap.addBundle(elideBundle);
    }

    @Override
    public void run(DropwizardElideConfiguration config, Environment environment) {
        environment.jersey().register(JsonApiEndpoint.class);
    }

    public static void main(String[] args) throws Exception {
//        new DropwizardElideApplication().run("server", "dropwizard-elide-example/example.yml");
        new DropwizardElideApplication().run(args);
    }

    @Override
    public String getName() {
        return name;
    }
}
