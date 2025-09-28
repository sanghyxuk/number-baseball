package com.couple.numberbaseball.service;

import com.couple.numberbaseball.model.GameSettings;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 숫자야구 게임 핵심 로직 서비스
 * - 정답 생성 (generate)
 * - 숫자 판정 (judge)
 * - 입력 검증
 */
@Service
public class GameLogicService {

    private final Random random = new Random();

    /**
     * 게임 설정에 따라 정답 숫자를 생성
     * @param settings 게임 설정 (자릿수, 0포함, 중복허용)
     * @return 생성된 정답 숫자 문자열
     */
    public String generateAnswer(GameSettings settings) {
        int digits = settings.getDigits();
        boolean allowZero = settings.isAllowZero();
        boolean allowDuplicate = settings.isAllowDuplicate();

        if (allowDuplicate) {
            return generateWithDuplicate(digits, allowZero);
        } else {
            return generateWithoutDuplicate(digits, allowZero);
        }
    }

    /**
     * 중복 허용하여 정답 생성
     */
    private String generateWithDuplicate(int digits, boolean allowZero) {
        StringBuilder answer = new StringBuilder();

        for (int i = 0; i < digits; i++) {
            if (i == 0 && !allowZero) {
                // 첫 번째 자리는 1~9
                answer.append(random.nextInt(9) + 1);
            } else {
                // 나머지 자리는 0~9 (allowZero에 따라)
                int maxDigit = allowZero ? 10 : 9;
                int startDigit = allowZero ? 0 : 1;
                answer.append(random.nextInt(maxDigit) + startDigit);
            }
        }

        return answer.toString();
    }

    /**
     * 중복 없이 정답 생성
     */
    private String generateWithoutDuplicate(int digits, boolean allowZero) {
        List<Integer> availableDigits = new ArrayList<>();

        // 사용 가능한 숫자 리스트 생성
        int start = allowZero ? 0 : 1;
        for (int i = start; i <= 9; i++) {
            availableDigits.add(i);
        }

        // 충분한 숫자가 있는지 확인
        if (availableDigits.size() < digits) {
            throw new IllegalArgumentException("중복 없이 " + digits + "자리 숫자를 만들 수 없습니다.");
        }

        StringBuilder answer = new StringBuilder();

        for (int i = 0; i < digits; i++) {
            if (i == 0 && !allowZero && availableDigits.contains(0)) {
                // 첫 번째 자리에 0이 올 수 없는 경우
                availableDigits.remove(Integer.valueOf(0));
                int randomIndex = random.nextInt(availableDigits.size());
                int selectedDigit = availableDigits.remove(randomIndex);
                answer.append(selectedDigit);
                // 0을 다시 추가 (다음 자리에서 사용 가능)
                availableDigits.add(0);
            } else {
                int randomIndex = random.nextInt(availableDigits.size());
                int selectedDigit = availableDigits.remove(randomIndex);
                answer.append(selectedDigit);
            }
        }

        return answer.toString();
    }

    /**
     * 추측과 정답을 비교하여 스트라이크/볼 판정
     * @param guess 추측 숫자
     * @param answer 정답 숫자
     * @return 판정 결과 (예: "1S 2B", "3S", "OUT")
     */
    public String judge(String guess, String answer) {
        if (guess == null || answer == null || guess.length() != answer.length()) {
            throw new IllegalArgumentException("추측과 정답의 길이가 다릅니다.");
        }

        int strikes = 0;
        int balls = 0;

        char[] guessChars = guess.toCharArray();
        char[] answerChars = answer.toCharArray();

        // 스트라이크 계산
        for (int i = 0; i < guessChars.length; i++) {
            if (guessChars[i] == answerChars[i]) {
                strikes++;
            }
        }

        // 볼 계산 (전체 일치하는 숫자 개수 - 스트라이크)
        boolean[] guessUsed = new boolean[guessChars.length];
        boolean[] answerUsed = new boolean[answerChars.length];

        // 스트라이크 위치 마킹
        for (int i = 0; i < guessChars.length; i++) {
            if (guessChars[i] == answerChars[i]) {
                guessUsed[i] = true;
                answerUsed[i] = true;
            }
        }

        // 볼 계산
        for (int i = 0; i < guessChars.length; i++) {
            if (!guessUsed[i]) {
                for (int j = 0; j < answerChars.length; j++) {
                    if (!answerUsed[j] && guessChars[i] == answerChars[j]) {
                        balls++;
                        answerUsed[j] = true;
                        break;
                    }
                }
            }
        }

        return formatJudgeResult(strikes, balls);
    }

    /**
     * 판정 결과를 문자열로 포맷팅
     */
    private String formatJudgeResult(int strikes, int balls) {
        if (strikes == 0 && balls == 0) {
            return "OUT";
        }

        StringBuilder result = new StringBuilder();
        if (strikes > 0) {
            result.append(strikes).append("S");
        }
        if (balls > 0) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(balls).append("B");
        }

        return result.toString();
    }

    /**
     * 입력된 숫자가 게임 설정에 유효한지 검증
     * @param input 입력 숫자
     * @param settings 게임 설정
     * @return 유효성 검증 결과
     */
    public boolean isValidInput(String input, GameSettings settings) {
        if (input == null || input.length() != settings.getDigits()) {
            return false;
        }

        // 숫자인지 확인
        if (!input.matches("\\d+")) {
            return false;
        }

        // 0 포함 여부 확인
        if (!settings.isAllowZero() && input.contains("0")) {
            return false;
        }

        // 첫 번째 자리가 0인지 확인 (0 허용이어도 첫 자리는 0이면 안됨)
        if (input.charAt(0) == '0') {
            return false;
        }

        // 중복 허용 여부 확인
        if (!settings.isAllowDuplicate()) {
            Set<Character> uniqueChars = new HashSet<>();
            for (char c : input.toCharArray()) {
                if (!uniqueChars.add(c)) {
                    return false; // 중복 발견
                }
            }
        }

        return true;
    }

    /**
     * 정답이 게임 설정에 맞게 유효한지 검증
     * (정답 설정 시 사용)
     */
    public boolean isValidAnswer(String answer, GameSettings settings) {
        return isValidInput(answer, settings);
    }
}