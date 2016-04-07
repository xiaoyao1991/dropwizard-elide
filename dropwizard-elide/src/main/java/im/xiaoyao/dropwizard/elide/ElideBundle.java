package im.xiaoyao.dropwizard.elide;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.google.common.collect.ImmutableList;
import com.yahoo.elide.Elide;
import com.yahoo.elide.audit.Slf4jLogger;
import com.yahoo.elide.datastores.hibernate5.HibernateStore;
import com.yahoo.elide.resources.JsonApiEndpoint;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.hibernate.SessionFactory;

/**
 * Created by xiaoyaoqian on 4/6/16.
 */
public abstract class ElideBundle <T extends Configuration> implements ConfiguredBundle<T>, DatabaseConfiguration<T> {
    public static final String DEFAULT_NAME = "elide-bundle";

    private SessionFactory sessionFactory;

    private final ImmutableList<Class<?>> entities;
    private final SessionFactoryFactory sessionFactoryFactory;

    public ElideBundle(Class<?> entity, Class<?>... entities) {
        this(ImmutableList.<Class<?>>builder().add(entity).add(entities).build(),
                new SessionFactoryFactory());
    }

    public ElideBundle(ImmutableList<Class<?>> entities,
                       SessionFactoryFactory sessionFactoryFactory) {
        this.entities = entities;
        this.sessionFactoryFactory = sessionFactoryFactory;
    }

    @Override
    public final void initialize(Bootstrap<?> bootstrap) {
        bootstrap.getObjectMapper().registerModule(createHibernate5Module());
    }

    protected Hibernate5Module createHibernate5Module() {
        return new Hibernate5Module();
    }

    protected String name() {
        return DEFAULT_NAME;
    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        final PooledDataSourceFactory dbConfig = getDataSourceFactory(configuration);
        sessionFactory = sessionFactoryFactory.build(this, environment, dbConfig, entities, name());

        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                JsonApiEndpoint.DefaultOpaqueUserFunction noUserFn = v -> null;

                bind(noUserFn)
                        .to(JsonApiEndpoint.DefaultOpaqueUserFunction.class)
                        .named("elideUserExtractionFunction");

                bind(new Elide.Builder(new Slf4jLogger(), new HibernateStore(sessionFactory)).build())
                        .to(Elide.class).named("elide");
            }
        });
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    protected void configure(org.hibernate.cfg.Configuration configuration) {
    }
}
