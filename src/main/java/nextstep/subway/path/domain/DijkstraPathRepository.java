package nextstep.subway.path.domain;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.domain.Section;
import nextstep.subway.path.exception.InvalidSourceTargetException;
import nextstep.subway.path.exception.PathNotFoundException;
import nextstep.subway.station.domain.Station;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;
import org.springframework.stereotype.Component;

@Component
public class DijkstraPathRepository implements PathRepository {
    private final LineRepository lineRepository;

    public DijkstraPathRepository(LineRepository lineRepository) {
        this.lineRepository = lineRepository;
    }

    @Override
    public Path findShortestPath(Station source, Station target) {
        if (source.equals(target)) {
            throw new InvalidSourceTargetException();
        }
        GraphPath<Station, DefaultWeightedEdge> graphPath = createDijkstra().getPath(source, target);
        if (graphPath == null) {
            throw new PathNotFoundException();
        }
        return new Path(graphPath.getVertexList(), graphPath.getLength());
    }

    private DijkstraShortestPath<Station, DefaultWeightedEdge> createDijkstra() {
        List<Section> sections = findAllSections();
        WeightedMultigraph<Station, DefaultWeightedEdge> graph = new WeightedMultigraph<>(DefaultWeightedEdge.class);
        addVertex(graph, sections);
        setEdgeWeight(graph, sections);
        return new DijkstraShortestPath(graph);
    }

    private void addVertex(WeightedMultigraph<Station, DefaultWeightedEdge> graph, List<Section> sections) {
        sections.stream()
                .map(section -> Arrays.asList(section.getUpStation(), section.getDownStation()))
                .flatMap(Collection::stream)
                .forEach(graph::addVertex);
    }

    private void setEdgeWeight(WeightedMultigraph<Station, DefaultWeightedEdge> graph, List<Section> sections) {
        for (Section section : sections) {
            DefaultWeightedEdge edge = graph.addEdge(section.getUpStation(), section.getDownStation());
            int weight = section.getDistance();
            graph.setEdgeWeight(edge, weight);
        }
    }

    private List<Section> findAllSections() {
        return lineRepository.findAll()
                .stream()
                .map(Line::getSections)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
