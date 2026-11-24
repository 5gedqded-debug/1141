package me.senkuro.unofficial.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Выпадение карточки в читалке — та же механика, что у сайта (composable `cardDrop`):
 * после конца главы через 1,5 с вызывается checkCardDrop; если canClaim — у края экрана появляется
 * карточка-рубашка, по тапу вызывается claimCardDrop и показывается окно с выпавшей картой.
 * Состояние общее для всего приложения (как provide/inject на сайте): карточка не пропадает при смене главы.
 */
class CardDropController(private val eco: EcosystemRepository) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val busy = AtomicBoolean(false)
    private val _canClaim = MutableStateFlow(false)
    val canClaim: StateFlow<Boolean> = _canClaim.asStateFlow()

    /** Проверка выпадения. Работает в своей области, чтобы смена главы не обрывала запрос. */
    fun check() {
        if (_canClaim.value || busy.get()) return
        scope.launch {
            delay(1500)
            if (_canClaim.value || !busy.compareAndSet(false, true)) return@launch
            try {
                if (eco.cardDropCheck()) _canClaim.value = true
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (_: Exception) {
                // как на сайте: ошибка проверки ничего не показывает
            } finally {
                busy.set(false)
            }
        }
    }

    /**
     * Забирает карточку. null — запрос уже идёт или сервер ничего не вернул. Ошибка сервера уходит исключением
     * (карточка остаётся доступной, текст показывается пользователю).
     */
    suspend fun claim(): CardDrop? {
        if (!busy.compareAndSet(false, true)) return null
        return try {
            // запрос не обрываем, даже если пользователь ушёл с экрана: иначе карточка выдастся, а окна не будет
            withContext(NonCancellable) {
                val drop = eco.cardDropClaim()
                _canClaim.value = false
                drop
            }
        } finally {
            busy.set(false)
        }
    }
}
