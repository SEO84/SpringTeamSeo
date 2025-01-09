// src/main/resources/static/js/create.js

/**
 * 추가 펫 선택 필드를 동적으로 생성하는 함수
 * - 사용자가 추가 반려동물을 선택할 수 있는 필드를 생성합니다.
 * - 이미 선택된 반려동물은 중복 선택을 방지합니다.
 */
function addPetSelection() {
    // 추가 펫 선택 필드가 추가될 컨테이너
    const container = document.getElementById('additionalPetsContainer');

    // 현재 선택된 반려동물 ID 가져오기 (중복 방지)
    const selectedPetIds = Array.from(
        document.querySelectorAll("select[name='additionalPetIds']")
    ).map(select => select.value);

    // 선택 가능한 반려동물 필터링
    const availablePets = userPets.filter(
        pet => !selectedPetIds.includes(pet.petId.toString())
    );

    // 선택 가능한 반려동물이 없는 경우 알림 표시 후 종료
    if (availablePets.length === 0) {
        alert("더 이상 선택할 수 있는 반려동물이 없습니다.");
        return;
    }

    // 새로운 펫 선택 필드를 담을 div 생성
    const petDiv = document.createElement('div');
    petDiv.className = 'form-group d-flex align-items-center';

    // 새로운 select 요소 생성
    const select = document.createElement('select');
    select.name = 'additionalPetIds';
    select.className = 'form-control mr-2';
    select.required = true;

    // 기본 옵션 추가 (선택 불가능 및 기본 선택 상태)
    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = '-- 반려동물 선택 --';
    defaultOption.disabled = true;
    defaultOption.selected = true;
    select.appendChild(defaultOption);

    // 선택 가능한 반려동물을 옵션으로 추가
    availablePets.forEach(pet => {
        const option = document.createElement('option');
        option.value = pet.petId;
        option.textContent = pet.name;
        select.appendChild(option);
    });

    // 삭제 버튼 생성
    const removeBtn = document.createElement('button');
    removeBtn.type = 'button';
    removeBtn.textContent = '삭제';
    removeBtn.className = 'btn btn-danger';
    // 삭제 버튼 클릭 시 해당 필드를 제거
    removeBtn.onclick = function () {
        container.removeChild(petDiv);
    };

    // div에 select와 삭제 버튼 추가
    petDiv.appendChild(select);
    petDiv.appendChild(removeBtn);
    // 컨테이너에 새로운 div 추가
    container.appendChild(petDiv);
}

/**
 * 이미지 미리보기 기능
 * - 사용자가 파일을 업로드하면 미리보기를 업데이트합니다.
 * @param {Event} event - 파일 선택 이벤트
 */
function previewImage(event) {
    // 파일 입력 요소 가져오기
    const fileInput = event.target;
    // 미리보기 이미지 요소 가져오기
    const preview = document.getElementById('imagePreview');

    // 파일이 선택된 경우 미리보기 업데이트
    if (fileInput.files && fileInput.files[0]) {
        const reader = new FileReader();
        reader.onload = function (e) {
            preview.src = e.target.result; // 미리보기 이미지에 파일 URL 설정
            preview.style.display = 'block'; // 이미지 표시
        };
        reader.readAsDataURL(fileInput.files[0]); // 파일 읽기 시작
    } else {
        // 파일이 없으면 미리보기 초기화
        preview.src = '';
        preview.style.display = 'none'; // 이미지 숨기기
    }
}
