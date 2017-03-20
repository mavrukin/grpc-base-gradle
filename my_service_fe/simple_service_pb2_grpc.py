import grpc
from grpc.framework.common import cardinality
from grpc.framework.interfaces.face import utilities as face_utilities

import google.protobuf.empty_pb2 as google_dot_protobuf_dot_empty__pb2
import google.protobuf.empty_pb2 as google_dot_protobuf_dot_empty__pb2
import simple_service_pb2 as simple__service__pb2
import simple_service_pb2 as simple__service__pb2
import simple_service_pb2 as simple__service__pb2
import simple_service_pb2 as simple__service__pb2
import simple_service_pb2 as simple__service__pb2
import google.protobuf.empty_pb2 as google_dot_protobuf_dot_empty__pb2
import simple_service_pb2 as simple__service__pb2
import simple_service_pb2 as simple__service__pb2
import simple_service_pb2 as simple__service__pb2
import simple_service_pb2 as simple__service__pb2


class SimpleStub(object):

  def __init__(self, channel):
    """Constructor.

    Args:
      channel: A grpc.Channel.
    """
    self.Noop = channel.unary_unary(
        '/grpc.Simple/Noop',
        request_serializer=google_dot_protobuf_dot_empty__pb2.Empty.SerializeToString,
        response_deserializer=google_dot_protobuf_dot_empty__pb2.Empty.FromString,
        )
    self.Echo = channel.unary_unary(
        '/grpc.Simple/Echo',
        request_serializer=simple__service__pb2.EchoRequest.SerializeToString,
        response_deserializer=simple__service__pb2.EchoResponse.FromString,
        )
    self.FailPlease = channel.unary_unary(
        '/grpc.Simple/FailPlease',
        request_serializer=simple__service__pb2.FailWithProbabilityOrSucceedEchoRequest.SerializeToString,
        response_deserializer=simple__service__pb2.EchoResponse.FromString,
        )


class SimpleServicer(object):

  def Noop(self, request, context):
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def Echo(self, request, context):
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def FailPlease(self, request, context):
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')


def add_SimpleServicer_to_server(servicer, server):
  rpc_method_handlers = {
      'Noop': grpc.unary_unary_rpc_method_handler(
          servicer.Noop,
          request_deserializer=google_dot_protobuf_dot_empty__pb2.Empty.FromString,
          response_serializer=google_dot_protobuf_dot_empty__pb2.Empty.SerializeToString,
      ),
      'Echo': grpc.unary_unary_rpc_method_handler(
          servicer.Echo,
          request_deserializer=simple__service__pb2.EchoRequest.FromString,
          response_serializer=simple__service__pb2.EchoResponse.SerializeToString,
      ),
      'FailPlease': grpc.unary_unary_rpc_method_handler(
          servicer.FailPlease,
          request_deserializer=simple__service__pb2.FailWithProbabilityOrSucceedEchoRequest.FromString,
          response_serializer=simple__service__pb2.EchoResponse.SerializeToString,
      ),
  }
  generic_handler = grpc.method_handlers_generic_handler(
      'grpc.Simple', rpc_method_handlers)
  server.add_generic_rpc_handlers((generic_handler,))


class LessSimpleStub(object):

  def __init__(self, channel):
    """Constructor.

    Args:
      channel: A grpc.Channel.
    """
    self.BlockForMillis = channel.unary_unary(
        '/grpc.LessSimple/BlockForMillis',
        request_serializer=simple__service__pb2.BlockForMillisRequest.SerializeToString,
        response_deserializer=google_dot_protobuf_dot_empty__pb2.Empty.FromString,
        )
    self.DoNEmptyRequests = channel.unary_unary(
        '/grpc.LessSimple/DoNEmptyRequests',
        request_serializer=simple__service__pb2.DoNEmptyRequestsRequest.SerializeToString,
        response_deserializer=simple__service__pb2.DoNEmptyRequestsResponse.FromString,
        )
    self.DoNRequestsAndFailSome = channel.unary_unary(
        '/grpc.LessSimple/DoNRequestsAndFailSome',
        request_serializer=simple__service__pb2.DoNEchoRequestsAndFailSomeRequest.SerializeToString,
        response_deserializer=simple__service__pb2.DoNEmptyRequestsResponse.FromString,
        )


class LessSimpleServicer(object):

  def BlockForMillis(self, request, context):
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def DoNEmptyRequests(self, request, context):
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')

  def DoNRequestsAndFailSome(self, request, context):
    context.set_code(grpc.StatusCode.UNIMPLEMENTED)
    context.set_details('Method not implemented!')
    raise NotImplementedError('Method not implemented!')


def add_LessSimpleServicer_to_server(servicer, server):
  rpc_method_handlers = {
      'BlockForMillis': grpc.unary_unary_rpc_method_handler(
          servicer.BlockForMillis,
          request_deserializer=simple__service__pb2.BlockForMillisRequest.FromString,
          response_serializer=google_dot_protobuf_dot_empty__pb2.Empty.SerializeToString,
      ),
      'DoNEmptyRequests': grpc.unary_unary_rpc_method_handler(
          servicer.DoNEmptyRequests,
          request_deserializer=simple__service__pb2.DoNEmptyRequestsRequest.FromString,
          response_serializer=simple__service__pb2.DoNEmptyRequestsResponse.SerializeToString,
      ),
      'DoNRequestsAndFailSome': grpc.unary_unary_rpc_method_handler(
          servicer.DoNRequestsAndFailSome,
          request_deserializer=simple__service__pb2.DoNEchoRequestsAndFailSomeRequest.FromString,
          response_serializer=simple__service__pb2.DoNEmptyRequestsResponse.SerializeToString,
      ),
  }
  generic_handler = grpc.method_handlers_generic_handler(
      'grpc.LessSimple', rpc_method_handlers)
  server.add_generic_rpc_handlers((generic_handler,))
