package com.likelion.olion.domain.capsule.service;

import com.likelion.olion.domain.capsule.dto.InspirationCapsuleHistoryResponse;
import com.likelion.olion.domain.capsule.dto.InspirationCapsuleOpenResponse;
import com.likelion.olion.domain.capsule.dto.InspirationCapsuleTodayResponse;
import com.likelion.olion.domain.capsule.entity.InspirationCapsule;
import com.likelion.olion.domain.capsule.repository.InspirationCapsuleRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InspirationCapsuleService {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int DAY_CUTOFF_HOUR = 4;

    private static final List<Quote> QUOTES = List.of(
            new Quote("새는 알에서 나오려고 투쟁한다. 알은 세계이다. 태어나려는 자는 하나의 세계를 깨뜨려야 한다.", "데미안"),
            new Quote("가장 중요한 것은 눈에 보이지 않아. 마음으로 보아야만 비로소 잘 보이지.", "어린 왕자"),
            new Quote("무언가를 간절히 원할 때, 온 우주는 자네의 소망이 실현되도록 도와준다네.", "연금술사"),
            new Quote("인간은 파멸할 수는 있어도 패배할 수는 없다.", "노인과 바다"),
            new Quote("마침내 나는 내 안에서 무적의 여름을 발견했다. 어떤 혹독한 겨울도 나를 이길 수 없었다.", "여름"),
            new Quote("누군가를 비판하고 싶어질 때면, 이 세상 사람이 다 너처럼 좋은 조건을 타고난 건 아니라는 걸 명심해라.", "위대한 개츠비"),
            new Quote("자기 자신이 아닌 다른 누군가가 되는 것보다 더 끔찍한 일은 없다.", "자기만의 방"),
            new Quote("우리가 겪는 유일한 삶은 단 한 번뿐이며, 그것은 마치 준비 없는 리허설과 같다.", "참을 수 없는 존재의 가벼움"),
            new Quote("자연과 햇빛, 자유가 있는 한 나는 결코 절망하지 않을 것이다.", "안네의 일기"),
            new Quote("자유란 2 더하기 2는 4라고 말할 수 있는 자유다.", "1984"),
            new Quote("인간에게서 모든 것을 빼앗아갈 수 있어도 단 한 가지, 어떠한 상황에서도 자신의 태도를 선택할 수 있는 자유는 빼앗아갈 수 없다.", "죽음의 수용소에서"),
            new Quote("시간은 삶이며, 삶은 우리 마음속에 있는 것이다.", "모모"),
            new Quote("엘리자가 말했어요. 세상은 생각대로 되지 않는다고. 하지만 생각대로 되지 않는다는 건 정말 멋진 일이에요. 생각지도 못했던 일이 일어나는 걸요!", "빨강 머리 앤"),
            new Quote("백지이기 때문에 어떤 지도라도 그릴 수 있습니다. 모든 것이 당신 하기 나름인 것이지요.", "나미야 잡화점의 기적"),
            new Quote("우리는 과거의 트라우마에 휘둘리는 것이 아니라, 미래의 목적을 향해 나아갈 수 있다.", "미움받을 용기"),
            new Quote("절망에 익숙해지는 것은 절망 그 자체보다 더 나쁘다.", "페스트"),
            new Quote("성공은 일상적인 습관의 결과이지, 인생에 한 번 있는 거대한 전환점의 결과가 아니다.", "아주 작은 습관의 힘"),
            new Quote("나는 아무것도 바라지 않는다. 나는 아무것도 두려워하지 않는다. 나는 자유다.", "그리스인 조르바"),
            new Quote("어떻게 죽어야 할지 배우면 어떻게 살아야 할지 배울 수 있다.", "모리와 함께한 화요일"),
            new Quote("내가 숲으로 간 이유는 의도적인 삶을 살기 위해서였다. 삶의 본질적인 사실들만을 직면해 보고 싶었다.", "월든"),
            new Quote("내 속에서 솟아 나오려는 것, 바로 그것을 나는 살아보려고 했다. 왜 그것이 그토록 어려웠을까.", "데미안"),
            new Quote("당신이 있는 곳은 과거의 결과이지만, 당신이 가는 곳은 전적으로 지금부터 당신이 누구가 되기로 선택하느냐에 달려 있다.", "미라클 모닝"),
            new Quote("박제가 되어버린 천재를 아시오", "날개"),
            new Quote("진정한 발견의 항해는 새로운 풍경을 찾는 것이 아니라, 새로운 눈을 가지는 데 있다.", "잃어버린 시간을 찾아서"),
            new Quote("길을 잃는다는 것은 곧 길을 안다는 것이다.", "크눌프"),
            new Quote("행복한 가정은 모두 엇비슷하고 불행한 가정은 불행한 이유가 제각기 다르다.", "안나 카레니나"),
            new Quote("인간은 노력하는 한 방황하는 법이다.", "파우스트"),
            new Quote("사막이 아름다운 건 어디엔가 우물이 숨어있기 때문이야.", "어린 왕자"),
            new Quote("상처는 빛이 당신에게 들어오는 곳이다.", "루미 시집"),
            new Quote("나는 내 인생이 아름다워질 것이라 기대하지 않는다. 오직 내 스스로 아름답게 만들기를 바랄 뿐이다.", "파리의 우울")
    );

    private final InspirationCapsuleRepository inspirationCapsuleRepository;
    private final SecureRandom random = new SecureRandom();

    public InspirationCapsuleService(InspirationCapsuleRepository inspirationCapsuleRepository) {
        this.inspirationCapsuleRepository = inspirationCapsuleRepository;
    }

    @Transactional(readOnly = true)
    public InspirationCapsuleTodayResponse getToday(Long userId) {
        Optional<InspirationCapsule> capsule = inspirationCapsuleRepository
                .findByUserIdAndOpenedDate(userId, today());
        return capsule
                .map(c -> new InspirationCapsuleTodayResponse(true, c.getQuoteText(), c.getBookTitle()))
                .orElseGet(() -> new InspirationCapsuleTodayResponse(false, null, null));
    }

    @Transactional
    public InspirationCapsuleOpenResponse open(Long userId) {
        LocalDate today = today();
        Optional<InspirationCapsule> existing = inspirationCapsuleRepository
                .findByUserIdAndOpenedDate(userId, today);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }
        Quote quote = QUOTES.get(random.nextInt(QUOTES.size()));
        InspirationCapsule capsule = new InspirationCapsule(userId, quote.text(), quote.bookTitle(), today);
        try {
            inspirationCapsuleRepository.saveAndFlush(capsule);
        } catch (DataIntegrityViolationException exception) {
            return inspirationCapsuleRepository.findByUserIdAndOpenedDate(userId, today)
                    .map(this::toResponse)
                    .orElseThrow(() -> exception);
        }
        return toResponse(capsule);
    }

    @Transactional(readOnly = true)
    public InspirationCapsuleHistoryResponse getHistory(Long userId) {
        return InspirationCapsuleHistoryResponse.from(
                inspirationCapsuleRepository.findByUserIdOrderByOpenedDateDesc(userId));
    }

    LocalDate today() {
        return ZonedDateTime.now(KST).minusHours(DAY_CUTOFF_HOUR).toLocalDate();
    }

    private InspirationCapsuleOpenResponse toResponse(InspirationCapsule capsule) {
        return new InspirationCapsuleOpenResponse(capsule.getQuoteText(), capsule.getBookTitle());
    }

    private record Quote(String text, String bookTitle) {
    }
}
