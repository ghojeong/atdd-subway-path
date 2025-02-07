package nextstep.subway.line.application;

import java.util.Arrays;
import java.util.Optional;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.domain.Section;
import nextstep.subway.line.dto.SectionRequest;
import nextstep.subway.line.exception.EmptyLineException;
import nextstep.subway.line.exception.InvalidDistanceException;
import nextstep.subway.line.exception.SectionAlreadyRegisteredException;
import nextstep.subway.line.exception.SectionNotSearchedException;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LineServiceMockTest {
    private static final int FIRST_INDEX = 0;

    @Mock
    private LineRepository lineRepository;
    @Mock
    private StationService stationService;

    @Autowired
    private LineService lineService;

    private Station 교대역;
    private Station 강남역;
    private Station 역삼역;
    private Station 선릉역;
    private Station 삼성역;
    private Line 이호선;

    @BeforeEach
    void setUp() {
        lineService = new LineService(lineRepository, stationService);

        교대역 = new Station("교대역");
        강남역 = new Station("강남역");
        역삼역 = new Station("역삼역");
        선릉역 = new Station("선릉역");
        삼성역 = new Station("삼성역");
        이호선 = new Line("2호선", "green", 강남역, 역삼역, 10);

        ReflectionTestUtils.setField(교대역, "id", 1L);
        ReflectionTestUtils.setField(강남역, "id", 2L);
        ReflectionTestUtils.setField(역삼역, "id", 3L);
        ReflectionTestUtils.setField(선릉역, "id", 4L);
        ReflectionTestUtils.setField(삼성역, "id", 5L);
        ReflectionTestUtils.setField(이호선, "id", 1L);
    }

    @DisplayName("노선에 구간을 추가하면, 노선의 크기가 증가")
    @Test
    void addSection() {
        // given
        // lineRepository, stationService stub 설정을 통해 초기값 셋팅
        when(stationService.findStationById(역삼역.getId())).thenReturn(역삼역);
        when(stationService.findStationById(삼성역.getId())).thenReturn(삼성역);
        when(lineRepository.findById(이호선.getId())).thenReturn(Optional.of(이호선));
        int expectedSize = 이호선.size() + 1;

        // when
        // lineService.addSection 호출
        int distance = 1;
        lineService.addSection(이호선.getId(), new SectionRequest(역삼역.getId(), 삼성역.getId(), distance));

        // then
        // line.findLineById 메서드를 통해 검증
        Line line = lineService.findLineById(이호선.getId());
        assertAll(
                () -> assertThat(line.size()).isEqualTo(expectedSize),
                () -> assertThat(line.getStations()).isEqualTo(Arrays.asList(강남역, 역삼역, 삼성역))
        );
    }

    @DisplayName("노선의 마지막 하행에 구간을 추가")
    @Test
    void addSectionLastDownStation() {
        // given
        when(stationService.findStationById(역삼역.getId())).thenReturn(역삼역);
        when(stationService.findStationById(선릉역.getId())).thenReturn(선릉역);
        when(lineRepository.findById(이호선.getId())).thenReturn(Optional.of(이호선));
        int expectedSize = 이호선.size() + 1;

        // when
        // lineService.addSection 호출
        int distance = 1;
        lineService.addSection(이호선.getId(), new SectionRequest(역삼역.getId(), 선릉역.getId(), distance));

        // then
        // line.findLineById 메서드를 통해 검증
        Line line = lineService.findLineById(이호선.getId());
        assertAll(
                () -> assertThat(line.size()).isEqualTo(expectedSize),
                () -> assertThat(line.getStations()).isEqualTo(Arrays.asList(강남역, 역삼역, 선릉역))
        );
    }

    @DisplayName("노선의 첫번째 상행에 구간을 추가")
    @Test
    void addSectionFirstUpStation() {
        // given
        when(stationService.findStationById(교대역.getId())).thenReturn(교대역);
        when(stationService.findStationById(강남역.getId())).thenReturn(강남역);
        when(lineRepository.findById(이호선.getId())).thenReturn(Optional.of(이호선));
        int expectedSize = 이호선.size() + 1;

        // when
        // lineService.addSection 호출
        int distance = 1;
        lineService.addSection(이호선.getId(), new SectionRequest(교대역.getId(), 강남역.getId(), distance));

        // then
        // line.findLineById 메서드를 통해 검증
        Line line = lineService.findLineById(이호선.getId());
        assertAll(
                () -> assertThat(line.size()).isEqualTo(expectedSize),
                () -> assertThat(line.getStations()).isEqualTo(Arrays.asList(교대역, 강남역, 역삼역))
        );
    }

    @DisplayName("노선의 중간에 구간을 추가")
    @Test
    void addSectionMiddle() {
        // given
        when(stationService.findStationById(강남역.getId())).thenReturn(강남역);
        when(stationService.findStationById(역삼역.getId())).thenReturn(역삼역);
        when(stationService.findStationById(선릉역.getId())).thenReturn(선릉역);
        when(stationService.findStationById(삼성역.getId())).thenReturn(삼성역);
        when(lineRepository.findById(이호선.getId())).thenReturn(Optional.of(이호선));
        int expectedSize = 이호선.size() + 2;

        // when
        // lineService.addSection 호출
        int distance = 1;
        lineService.addSection(이호선.getId(), new SectionRequest(강남역.getId(), 선릉역.getId(), distance));
        lineService.addSection(이호선.getId(), new SectionRequest(삼성역.getId(), 역삼역.getId(), distance));

        // then
        // line.findLineById 메서드를 통해 검증
        Line line = lineService.findLineById(이호선.getId());
        assertAll(
                () -> assertThat(line.size()).isEqualTo(expectedSize),
                () -> assertThat(line.getStations()).isEqualTo(Arrays.asList(강남역, 선릉역, 삼성역, 역삼역))
        );
    }

    @DisplayName("노선의 중간에 구간을 추가 시, 구간의 길이가 노선의 길이 이상이면 에러 발생")
    @ValueSource(ints = {10, 11, 100})
    @ParameterizedTest
    void addSectionMiddleInvalidDistance(int distance) {
        // given
        // lineRepository, stationService stub 설정을 통해 초기값 셋팅
        when(stationService.findStationById(역삼역.getId())).thenReturn(역삼역);
        when(stationService.findStationById(선릉역.getId())).thenReturn(선릉역);
        when(lineRepository.findById(이호선.getId())).thenReturn(Optional.of(이호선));

        // when, then
        assertThatExceptionOfType(InvalidDistanceException.class)
                .isThrownBy(() -> lineService.addSection(이호선.getId(), new SectionRequest(선릉역.getId(), 역삼역.getId(), distance)));
    }

    @DisplayName("상행역과 하행역이 이미 모선에 모두 등록되어 있다면 추가 시 에러 발생")
    @Test
    void addSectionAlreadyRegistered() {
        // given
        when(stationService.findStationById(강남역.getId())).thenReturn(강남역);
        when(stationService.findStationById(역삼역.getId())).thenReturn(역삼역);
        when(lineRepository.findById(이호선.getId())).thenReturn(Optional.of(이호선));

        // when, then
        int distance = 1;
        assertAll(
                () -> assertThatExceptionOfType(SectionAlreadyRegisteredException.class)
                        .isThrownBy(() -> lineService.addSection(이호선.getId(), new SectionRequest(강남역.getId(), 역삼역.getId(), distance))),
                () -> assertThatExceptionOfType(SectionAlreadyRegisteredException.class)
                        .isThrownBy(() -> lineService.addSection(이호선.getId(), new SectionRequest(역삼역.getId(), 강남역.getId(), distance)))
        );
    }

    @DisplayName("상행역과 하행역 둘 중 하나도 포함되어있지 않으면 추가 시 에러 발생")
    @Test
    void addSectionNotSearched() {
        // given
        when(stationService.findStationById(교대역.getId())).thenReturn(교대역);
        when(stationService.findStationById(삼성역.getId())).thenReturn(삼성역);
        when(lineRepository.findById(이호선.getId())).thenReturn(Optional.of(이호선));

        // when, then
        int distance = 1;
        assertThatExceptionOfType(SectionNotSearchedException.class)
                .isThrownBy(() -> lineService.addSection(이호선.getId(), new SectionRequest(교대역.getId(), 삼성역.getId(), distance)));
    }

    @DisplayName("노선에서 구간을 삭제하면, 구간의 길이가 병합")
    @Test
    void removeSection() {
        // given
        // lineRepository, stationService stub 설정을 통해 초기값 셋팅
        when(stationService.findStationById(교대역.getId())).thenReturn(교대역);
        when(stationService.findStationById(강남역.getId())).thenReturn(강남역);
        when(stationService.findStationById(역삼역.getId())).thenReturn(역삼역);
        when(stationService.findStationById(선릉역.getId())).thenReturn(선릉역);
        when(stationService.findStationById(삼성역.getId())).thenReturn(삼성역);
        when(lineRepository.findById(이호선.getId())).thenReturn(Optional.of(이호선));

        int upDistance = 3;
        int downDistance = 1;
        lineService.addSection(이호선.getId(), new SectionRequest(역삼역.getId(), 삼성역.getId(), upDistance));
        lineService.addSection(이호선.getId(), new SectionRequest(교대역.getId(), 강남역.getId(), downDistance));
        lineService.addSection(이호선.getId(), new SectionRequest(선릉역.getId(), 삼성역.getId(), downDistance));
        int expectedSize = 이호선.size() - 1;
        int expectedDistance = 3;

        // when
        lineService.removeSection(이호선.getId(), 선릉역.getId());

        // then
        Line line = lineService.findLineById(이호선.getId());
        Section mergedSection = line.getSections().get(line.getSections().size() - 1);
        assertAll(
                () -> assertThat(mergedSection.getDistance()).isEqualTo(expectedDistance),
                () -> assertThat(line.size()).isEqualTo(expectedSize),
                () -> assertThat(line.getStations()).isEqualTo(Arrays.asList(교대역, 강남역, 역삼역, 삼성역))
        );
    }

    @DisplayName("노선의 마지막 하행을 삭제")
    @Test
    void removeLastDownStation() {
        // given
        // lineRepository, stationService stub 설정을 통해 초기값 셋팅
        when(stationService.findStationById(역삼역.getId())).thenReturn(역삼역);
        when(stationService.findStationById(선릉역.getId())).thenReturn(선릉역);
        when(lineRepository.findById(이호선.getId())).thenReturn(Optional.of(이호선));

        int distance = 1;
        lineService.addSection(이호선.getId(), new SectionRequest(역삼역.getId(), 선릉역.getId(), distance));
        int expectedSize = 이호선.size() - 1;

        // when
        lineService.removeSection(이호선.getId(), 선릉역.getId());

        // then
        Line line = lineService.findLineById(이호선.getId());
        assertAll(
                () -> assertThat(line.size()).isEqualTo(expectedSize),
                () -> assertThat(line.getStations()).isEqualTo(Arrays.asList(강남역, 역삼역))
        );
    }

    @DisplayName("노선의 첫번째 상행을 삭제")
    @Test
    void removeFirstUpStation() {
        // given
        // lineRepository, stationService stub 설정을 통해 초기값 셋팅
        when(stationService.findStationById(강남역.getId())).thenReturn(강남역);
        when(stationService.findStationById(역삼역.getId())).thenReturn(역삼역);
        when(stationService.findStationById(선릉역.getId())).thenReturn(선릉역);
        when(lineRepository.findById(이호선.getId())).thenReturn(Optional.of(이호선));

        int distance = 1;
        lineService.addSection(이호선.getId(), new SectionRequest(역삼역.getId(), 선릉역.getId(), distance));
        int expectedSize = 이호선.size() - 1;

        // when
        lineService.removeSection(이호선.getId(), 강남역.getId());

        // then
        Line line = lineService.findLineById(이호선.getId());
        assertAll(
                () -> assertThat(line.size()).isEqualTo(expectedSize),
                () -> assertThat(line.getStations()).isEqualTo(Arrays.asList(역삼역, 선릉역))
        );
    }

    @DisplayName("노선의 중간을 삭제")
    @Test
    void removeMiddleStation() {
        // given
        // lineRepository, stationService stub 설정을 통해 초기값 셋팅
        when(stationService.findStationById(역삼역.getId())).thenReturn(역삼역);
        when(stationService.findStationById(선릉역.getId())).thenReturn(선릉역);
        when(lineRepository.findById(이호선.getId())).thenReturn(Optional.of(이호선));

        int distance = 1;
        lineService.addSection(이호선.getId(), new SectionRequest(역삼역.getId(), 선릉역.getId(), distance));
        int expectedSize = 이호선.size() - 1;

        // when
        lineService.removeSection(이호선.getId(), 역삼역.getId());

        // then
        Line line = lineService.findLineById(이호선.getId());
        assertAll(
                () -> assertThat(line.size()).isEqualTo(expectedSize),
                () -> assertThat(line.getStations()).isEqualTo(Arrays.asList(강남역, 선릉역))
        );
    }

    @DisplayName("구간이 하나인 노선에서 역 삭제 시 에러 발생")
    @Test
    void removeSectionEmptyLine() {
        // given
        // lineRepository, stationService stub 설정을 통해 초기값 셋팅
        when(stationService.findStationById(삼성역.getId())).thenReturn(삼성역);
        when(lineRepository.findById(이호선.getId())).thenReturn(Optional.of(이호선));

        // then
        assertThatExceptionOfType(EmptyLineException.class)
                .isThrownBy(() -> lineService.removeSection(이호선.getId(), 삼성역.getId()));
    }

    @DisplayName("노선에 존재하지 않는 역을 가진 구역을 삭제시 에러 발생")
    @Test
    void removeSectionStationNotFound() {
        // given
        // lineRepository, stationService stub 설정을 통해 초기값 셋팅
        when(stationService.findStationById(교대역.getId())).thenReturn(교대역);
        when(stationService.findStationById(역삼역.getId())).thenReturn(역삼역);
        when(stationService.findStationById(선릉역.getId())).thenReturn(선릉역);
        when(lineRepository.findById(이호선.getId())).thenReturn(Optional.of(이호선));

        int distance = 1;
        lineService.addSection(이호선.getId(), new SectionRequest(역삼역.getId(), 선릉역.getId(), distance));

        // then
        assertThatExceptionOfType(SectionNotSearchedException.class)
                .isThrownBy(() -> lineService.removeSection(이호선.getId(), 교대역.getId()));
    }
}
