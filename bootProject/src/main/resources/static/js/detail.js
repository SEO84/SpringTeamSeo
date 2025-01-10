// document.addEventListener('DOMContentLoaded', function () {
//     /**
//      * 카카오맵 초기화 함수
//      * @param {string} address - 매칭방의 실제 장소 주소
//      */
//     function initMap(address) {
//         if (typeof kakao === 'undefined') {
//             console.error('카카오맵 API를 로드할 수 없습니다.');
//             return;
//         }
//
//         const mapContainer = document.querySelector('.map-placeholder');
//         const geocoder = new kakao.maps.services.Geocoder();
//
//         // 주소 검색 및 지도 초기화
//         geocoder.addressSearch(address, function (result, status) {
//             if (status === kakao.maps.services.Status.OK) {
//                 const coords = new kakao.maps.LatLng(result[0].y, result[0].x);
//
//                 const map = new kakao.maps.Map(mapContainer, {
//                     center: coords,
//                     level: 3
//                 });
//
//                 const marker = new kakao.maps.Marker({
//                     position: coords
//                 });
//                 marker.setMap(map);
//
//                 const infowindow = new kakao.maps.InfoWindow({
//                     content: `<div style="padding:5px;">${address}</div>`
//                 });
//                 infowindow.open(map, marker);
//             } else {
//                 console.error('주소 검색 실패:', status);
//                 showError(mapContainer, '주소를 찾을 수 없습니다.');
//             }
//         });
//     }
//     function confirmAndDelete(event, roomId) {
//         event.preventDefault();
//         if (!confirm('정말로 삭제하시겠습니까?')) {
//             return false;
//         }
//
//         fetch(`/matching/delete/${roomId}`, {
//             method: 'DELETE',
//             headers: {
//                 'Content-Type': 'application/json',
//             },
//         }).then(response => {
//             if (response.ok) {
//                 alert('매칭방이 성공적으로 삭제되었습니다.');
//                 window.location.href = '/matching/list';
//             } else {
//                 alert('삭제 중 문제가 발생했습니다.');
//             }
//         }).catch(error => {
//             console.error('Error during deletion:', error);
//             alert('삭제 중 오류가 발생했습니다.');
//         });
//
//         return false;
//     }
//
//     /**
//      * 에러 메시지를 지도 영역에 표시
//      * @param {Element} container - 지도 컨테이너 요소
//      * @param {string} message - 표시할 에러 메시지
//      */
//     function showError(container, message) {
//         container.innerHTML = `<div style="color: red; text-align: center; padding: 20px;">${message}</div>`;
//     }
//
//     // 매칭방 장소 주소 (서버에서 전달받는 데이터)
//     const place = /*[[${room.place}]]*/ '서울특별시';
//
//     // 지도 초기화 호출
//     initMap(place);
// });
