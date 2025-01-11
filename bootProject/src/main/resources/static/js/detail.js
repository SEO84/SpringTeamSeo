// src/main/resources/static/js/detail.js

// 호스트 - 참여관리 모달
function openParticipantManageModal() {
    const modal = document.getElementById('participantManageModal');
    modal.style.display = 'flex';
}
function closeParticipantManageModal() {
    const modal = document.getElementById('participantManageModal');
    modal.style.display = 'none';
}

// 유저 - 펫 선택 모달
function openPetSelectModal() {
    const modal = document.getElementById('petSelectModal');
    modal.style.display = 'flex';
}
function closePetSelectModal() {
    const modal = document.getElementById('petSelectModal');
    modal.style.display = 'none';
}

// 지도 예시 (카카오)
document.addEventListener('DOMContentLoaded', function(){
    function initMap(address) {
        if (typeof kakao === 'undefined') {
            console.error('카카오맵 API 로드 실패');
            return;
        }
        const container = document.querySelector('.map-placeholder');
        const geocoder = new kakao.maps.services.Geocoder();
        geocoder.addressSearch(address, function(result, status){
            if(status === kakao.maps.services.Status.OK){
                const coords = new kakao.maps.LatLng(result[0].y, result[0].x);
                const map = new kakao.maps.Map(container, { center: coords, level: 3 });
                const marker = new kakao.maps.Marker({ position: coords });
                marker.setMap(map);
            } else {
                container.innerHTML = '<div style="color:red">주소를 찾을 수 없습니다</div>';
            }
        });
    }

    const place = /*[[${room.place}]]*/ '서울';
    initMap(place);
});
