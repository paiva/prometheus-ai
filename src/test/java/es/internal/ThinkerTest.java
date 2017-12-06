package es.internal;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import tags.Fact;
import tags.Predicate;
import tags.Recommendation;
import tags.Rule;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

public class ThinkerTest {
    private Thinker thinker;
    private ThinkCycleExecutor thinkCycleExecutor;
    private Set<Rule> readyRules;
    private Set<Rule> activeRules;
    private Set<Fact> facts;
    private Set<Recommendation> recommendations;

    @BeforeMethod
    public void setUp() throws Exception {
        readyRules = new HashSet<>();
        activeRules = new HashSet<>();
        facts = new HashSet<>();
        recommendations = new HashSet<>();
        thinkCycleExecutor = mock(ThinkCycleExecutor.class);
        ThinkCycleExecutorFactory thinkCycleExecutorFactory = mock(ThinkCycleExecutorFactory.class);
        when(thinkCycleExecutorFactory.create(readyRules, activeRules, facts, recommendations))
                .thenReturn(thinkCycleExecutor);
        thinker = new Thinker(readyRules, activeRules, facts, recommendations, thinkCycleExecutorFactory);
    }

    @Test
    public void mustThink() throws Exception {
        Recommendation recommendation = mock(Recommendation.class);
        Set<Predicate> activatedPredicates = new HashSet<>(Arrays.asList(
                recommendation,
                mock(Fact.class)
        ));
        Set<Recommendation> expectedActivatedRecommendations = Collections.singleton(recommendation);

        // given
        when(thinkCycleExecutor.thinkCycle()).thenReturn(activatedPredicates);

        // when
        Set<Recommendation> actualActivatedRecommendations = thinker.think(false, 1);

        // then
        assertEquals(expectedActivatedRecommendations, actualActivatedRecommendations);
    }

}