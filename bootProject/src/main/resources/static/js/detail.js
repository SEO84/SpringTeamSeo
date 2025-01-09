// src/main/resources/static/js/detail.js

// **참여 관리 모달 열기**: 호스트가 '참여 관리' 버튼을 클릭하면 모달을 열도록 설정
function openParticipantManageModal() {
    const modal = document.getElementById('participantManageModal');
    modal.style.display = 'flex'; // 모달을 보이게 설정 (CSS flex 사용)
}

// **참여 관리 모달 닫기**: 모달 창을 닫는 동작
function closeParticipantManageModal() {
    const modal = document.getElementById('participantManageModal');
    modal.style.display = 'none'; // 모달을 숨김
}

// **펫 선택 모달 열기**: 일반 사용자가 '신청' 버튼을 클릭하면 펫 선택 모달을 열도록 설정
function openPetSelectModal() {
    const modal = document.getElementById('petSelectModal');
    modal.style.display = 'flex'; // 모달을 보이게 설정 (CSS flex 사용)
}

// **펫 선택 모달 닫기**: 펫 선택 모달을 닫는 동작
function closePetSelectModal() {
    const modal = document.getElementById('petSelectModal');
    modal.style.display = 'none'; // 모달을 숨김
}

// **카카오 지도 초기화**: 매칭방의 장소 정보를 기반으로 지도 표시
document.addEventListener('DOMContentLoaded', function () {
    /**
     * **initMap 함수**: 주소를 받아 지도에 해당 위치를 표시하는 함수
     * @param {string} address - 검색할 주소
     */
    function initMap(address) {
        // 카카오맵 API 로드 여부 확인
        if (typeof kakao === 'undefined') {
            console.error('카카오맵 API 로드 실패');
            return;
        }

        // 지도 표시를 위한 컨테이너 및 지오코더 생성
        const container = document.querySelector('.map-placeholder');
        const geocoder = new kakao.maps.services.Geocoder();

        // 주소 검색
        geocoder.addressSearch(address, function (result, status) {
            if (status === kakao.maps.services.Status.OK) {
                // 주소 검색 성공 시 좌표 반환
                const coords = new kakao.maps.LatLng(result[0].y, result[0].x);

                // 지도 초기화
                const map = new kakao.maps.Map(container, {
                    center: coords, // 지도 중심 좌표 설정
                    level: 3,       // 지도 줌 레벨 설정
                });

                // 마커 표시
                const marker = new kakao.maps.Marker({
                    position: coords, // 마커 위치 설정
                });
                marker.setMap(map); // 지도에 마커 추가
            } else {
                // 주소 검색 실패 시 메시지 출력
                container.innerHTML = '<div style="color:red">주소를 찾을 수 없습니다</div>';
            }
        });
    }

    // **주소 변수 초기화**
    const place = /*[[${room.place}]]*/ '서울'; // Thymeleaf 변수를 JavaScript로 전달
    initMap(place); // 지도 초기화 함수 호출
});
