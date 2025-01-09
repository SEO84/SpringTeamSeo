document.addEventListener('DOMContentLoaded', function () {
    // 태그 클릭 이벤트
    document.querySelectorAll('.keyword-tag').forEach(tag => {
        tag.addEventListener('click', function () {
            const keyword = this.innerText;
            alert(`"${keyword}" 키워드로 검색합니다.`);
            // 여기서 검색 API 호출 로직 추가
        });
    });

    // 검색 폼 제출 이벤트
    const searchForm = document.querySelector('.search-form');
    searchForm.addEventListener('submit', function (e) {
        e.preventDefault();
        const keyword = searchForm.querySelector('.search-input').value;
        alert(`"${keyword}" 검색 중...`);
        // 여기서 검색 API 호출 로직 추가
    });
});
