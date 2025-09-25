package com.echo.verse.app.controller;

import com.echo.verse.app.dto.req.CreateConversationReqDTO;
import com.echo.verse.app.dto.req.SendMessageReqDTO;
import com.echo.verse.app.dto.resp.ConversationInfoRespDTO;
import com.echo.verse.app.dto.resp.ConversationRespDTO;
import com.echo.verse.app.dto.resp.MessageRespDTO;
import com.echo.verse.app.security.UserPrincipal;
import com.echo.verse.app.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author hpk
 */
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversation API", description = "AI对话核心接口")
@SecurityRequirement(name = "bearerAuth")
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    @Operation(summary = "创建新对话")
    public Mono<ResponseEntity<ConversationRespDTO>> startConversation(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateConversationReqDTO reqDTO) {
        return conversationService.startNewConversation(principal.getId(), reqDTO)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    @Operation(summary = "获取历史会话列表")
    public Flux<ConversationInfoRespDTO> getConversationHistory(@AuthenticationPrincipal UserPrincipal principal) {
        return conversationService.getConversationHistory(principal.getId());
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "获取指定会话的消息历史")
    public Flux<MessageRespDTO> getMessageHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String conversationId) {
        return conversationService.getMessageHistory(principal.getId(), conversationId);
    }

    @PostMapping(value = "/{id}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "发送消息并获取流式响应")
    public Flux<ServerSentEvent<String>> streamMessage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String conversationId,
            @RequestBody SendMessageReqDTO reqDTO) {
        return conversationService.streamChatMessage(principal.getId(), conversationId, reqDTO);
    }

    @GetMapping(value = "/{conversationId}/messages/{messageId}/audio", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "获取AI消息的语音")
    public Mono<ResponseEntity<byte[]>> getMessageAudio(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String conversationId,
            @PathVariable Long messageId) {
        return conversationService.synthesizeMessageAudio(principal.getId(), conversationId, messageId)
                .map(audioBytes -> ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(audioBytes));
    }
}
