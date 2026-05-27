import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { QueueModule } from './queue/queue.module';
import { PdfGeneratorModule } from './pdf-generator/pdf-generator.module';
import { BlockchainModule } from './blockchain/blockchain.module';
import { WebhooksModule } from './webhooks/webhooks.module';

@Module({
  imports: [QueueModule, PdfGeneratorModule, BlockchainModule, WebhooksModule],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
